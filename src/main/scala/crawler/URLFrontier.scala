package crawler

import fetcher._
import java.io.PrintWriter
import java.net.URL
import robotstxt._
import scala.collection.concurrent
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success}

/**
 * @author andrei
 */
final class URLFrontier(
    configuration: CrawlConfiguration,
    initial: Traversable[String]) {
  private val maximumHistorySize = 1000000
  private val maximumSize = 100000
  private val fetcher = PageFetcher(
    configuration.userAgentString,
    configuration.followRedirects,
    configuration.connectionTimeout,
    configuration.requestTimeout)
  private val parser = RobotstxtParser(configuration.agentName)
  private val rules = concurrent.TrieMap[String, RuleSet]()
  private val queue = concurrent.TrieMap[String, Unit]()
  private val history = concurrent.TrieMap[String, Unit]()
  private val logger = new PrintWriter("logs.log")

  initial.foreach(tryPush)

  private def push(url: String): Unit = synchronized {
    while (history.size >= maximumHistorySize)
      history -= history.head._1
    history += url -> ()
    while (queue.size >= maximumSize)
      queue -= queue.head._1
    queue += url -> ()
    logger.println("Enqueued " + url)
  }

  def isAllowed(urlString: String): Future[Boolean] = Future {
    if (!configuration.urlFilter(urlString) || history.contains(urlString))
      false
    else {
      val URL = Try(new URL(urlString))
      for {
        url <- URL
        if !rules.contains(url.getHost)
        link = url.getProtocol + "://" + url.getHost + "/robots.txt"
        robots = Try(Await.result(fetcher.fetch(link), Duration.Inf))
        byteContent = robots.map(_.content).getOrElse(Array[Byte]())
        content = new String(byteContent, "UTF-8")
        if content.length <= configuration.maxRobotsSize
      } {
        synchronized(logger.println("Got robots for " + link))
        val ruleSet = Try(parser.getRules(content)).getOrElse(RuleSet.empty)
        rules += (url.getHost -> ruleSet)
      }
      (for {
        url <- URL
        ruleSet <- Try(rules(url.getHost))
        allowed <- Try(ruleSet.isAllowed(url.getPath))
      } yield allowed).getOrElse(true)
    }
  }

  def tryPush(urlString: String): Unit = {
    isAllowed(urlString).onComplete {
      case Success(allowed) =>
        if (allowed)
          push(urlString)
        else
          synchronized(logger.println("Access denied by REP to " + urlString))
      case _ => ()
    }
  }

  def pop(): Future[String] = synchronized {
    val link = queue.head._1
    queue -= link
    logger.println("Dequeued " + link)
    URLFrontier.delay(link, rules.getOrElse(link, RuleSet.empty).delay.seconds)
  }

  def isEmpty: Boolean = queue.isEmpty

  def size: Int = queue.size
}

object URLFrontier {
  def apply(configuration: CrawlConfiguration, initial: String*): URLFrontier =
    new URLFrontier(configuration, initial)

  def apply(
      configuration: CrawlConfiguration,
      initial: Traversable[String]): URLFrontier =
    new URLFrontier(configuration, initial)

  private def never[T]: Future[T] = {
    val p = Promise[T]()
    p.future
  }

  private def delay[T](block: => T, duration: Duration): Future[T] = {
    val p = Promise[T]()
    Future(Await.ready(never, duration))
      .onComplete(f => p.complete(Try(block)))
    p.future
  }
}
