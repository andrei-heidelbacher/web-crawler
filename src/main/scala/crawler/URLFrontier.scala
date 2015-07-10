package crawler

import fetcher._
import java.io.PrintWriter
import java.net.URL
import robotstxt._
import scala.collection.concurrent
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * @author andrei
 */
final class URLFrontier(
    configuration: CrawlConfiguration,
    initial: Traversable[String]) {
  private val maximumSize = 100000
  private val fetcher = PageFetcher(
    configuration.userAgentString,
    configuration.followRedirects,
    configuration.connectionTimeout,
    configuration.requestTimeout)
  private val parser = RobotstxtParser(configuration.agentName)
  private val rules = concurrent.TrieMap[String, RuleSet]()
  private val queue = concurrent.TrieMap[String, Unit]()
  private val logger = new PrintWriter("logs.log")

  initial.foreach(tryPush)

  private def push(url: String): Unit = synchronized {
    while (queue.size >= maximumSize)
      queue -= queue.head._1
    queue += url -> ()
    logger.println("Enqueued " + url)
  }

  def tryPush(urlString: String): Unit = {
    if (configuration.urlFilter(urlString)) {
      val URL = Try(new URL(urlString))
      for {
        url <- URL
        if !rules.contains(url.getHost)
        link = url.getProtocol + "://" + url.getHost + "/robots.txt"
        robots = Try(Await.result(fetcher.fetch(link), Duration.Inf))
        content = robots.map(_.content).getOrElse(Array[Byte]())
      } {
        synchronized(logger.println("Got robots for " + link))
        val ruleSet = Try(parser.getRules(new String(content, "UTF-8")))
          .getOrElse(RuleSet.empty)
        rules += (url.getHost -> ruleSet)
      }
      val allowed = (for {
        url <- URL
        ruleSet <- Try(rules(url.getHost))
        isAllowed <- Try(ruleSet.isAllowed(url.getPath))
      } yield isAllowed).getOrElse(true)
      if (allowed)
        push(urlString)
      else
        synchronized(logger.println("Access denied by REP to " + urlString))
    }
  }

  def pop(): Future[String] = synchronized {
    val link = queue.head._1
    queue -= link
    logger.println("Dequeued " + link)
    //URLFrontier.delay(Future.successful(link), 0.seconds)
    Future.successful(link)
    URLFrontier.delay(link, 0.seconds)
  }

  def isEmpty: Boolean = synchronized(queue.isEmpty)

  def size: Int = synchronized(queue.size)
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

  private def delay[T](future: Future[T], duration: Duration): Future[T] = {
    Future(Await.result(never[T], duration)).recoverWith({ case _ => future })
  }
}
