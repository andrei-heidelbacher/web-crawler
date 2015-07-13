package crawler

import fetcher._
import robotstxt._

import java.io.PrintWriter
import java.net.URL

import scala.collection.concurrent
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

/**
 * @author andrei
 */
final class URLFrontier(
    configuration: CrawlConfiguration,
    initial: Traversable[URL]) {
  private val maximumHistorySize = 1000000
  private val maximumSize = 100000
  private val fetcher = PageFetcher(
    configuration.userAgentString,
    configuration.followRedirects,
    configuration.connectionTimeoutInMs,
    configuration.requestTimeoutInMs)
  private val parser = RobotstxtParser(configuration.agentName)
  private val rules = concurrent.TrieMap[String, RuleSet]()
  private val queue = concurrent.TrieMap[URL, Unit]()
  private val history = concurrent.TrieMap[URL, Unit]()
  private val logger = new PrintWriter("logs.log")

  initial.foreach(tryPush)

  private def push(url: URL): Unit = synchronized {
    while (history.size >= maximumHistorySize)
      history -= history.head._1
    history += url -> ()
    while (queue.size >= maximumSize)
      queue -= queue.head._1
    queue += url -> ()
    logger.println("Enqueued " + url)
  }

  private def fetchRobots(url: URL): Future[RuleSet] = Future {
    val link = url.getProtocol + "://" + url.getHost + "/robots.txt"
    val robots = Try(Await.result(fetcher.fetch(link), Duration.Inf))
    val byteContent = robots.map(_.content).getOrElse(Array[Byte]())
    val content = new String(byteContent, "UTF-8")
    if (byteContent.length <= configuration.maxRobotsSize)
      Try(parser.getRules(content)).getOrElse(RuleSet.empty)
    else
      RuleSet.empty
  }

  def isAllowed(url: URL): Future[Boolean] = Future {
    if (!configuration.urlFilter(url) || history.contains(url))
      false
    else {
      if (!rules.contains(url.getHost)) {
        fetchRobots(url).onComplete {
          case Success(ruleSet) =>
            rules += (url.getHost -> ruleSet)
            synchronized(logger.println("Got robots for " + url.getHost))
          case _ => ()
        }
      }
      (for {
        ruleSet <- Try(rules(url.getHost))
        allowed <- Try(ruleSet.isAllowed(url.getPath))
      } yield allowed).getOrElse(true)
    }
  }

  def tryPush(url: URL): Unit = {
    isAllowed(url).onComplete {
      case Success(allowed) =>
        if (allowed)
          push(url)
        else
          synchronized(logger.println("Access denied by REP to " + url))
      case _ => ()
    }
  }

  def pop(): Future[URL] = synchronized {
    val link = queue.head._1
    queue -= link
    logger.println("Dequeued " + link)
    URLFrontier.delay(
      link,
      rules.getOrElse(link.getHost, RuleSet.empty).delay.seconds)
  }

  def isEmpty: Boolean = queue.isEmpty

  def size: Int = queue.size
}

object URLFrontier {
  def apply(configuration: CrawlConfiguration, initial: URL*): URLFrontier =
    new URLFrontier(configuration, initial)

  def apply(
      configuration: CrawlConfiguration,
      initial: Traversable[URL]): URLFrontier =
    new URLFrontier(configuration, initial)

  private def never[T]: Future[T] = {
    val p = Promise[T]()
    p.future
  }

  private def delay[T](block: => T, duration: Duration): Future[T] = {
    val p = Promise[T]()
    Future(Await.ready(never, duration)).onComplete(f => p.complete(Try(block)))
    p.future
  }
}
