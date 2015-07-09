package crawler

import scala.collection.concurrent
import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Try
import java.net.URL
import robotstxt._
import fetcher._

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

  initial.foreach(tryPush)

  private def push(url: String): Unit = synchronized {
    while (queue.size >= maximumSize)
      queue -= queue.head._1
    queue += url -> ()
  }

  def tryPush(urlString: String): Unit = {
    if (configuration.urlFilter(urlString)) {
      for {
        url <- Try(new URL(urlString))
        if !rules.contains(url.getHost)
        link = url.getProtocol + "://" + url.getHost + "/robots.txt"
        robots = Try(Await.result(fetcher.fetch(link), Duration.Inf))
        content = robots.map(_.content).getOrElse(Array[Byte]())
      } {
        rules += (url.getHost -> parser.getRules(new String(content, "UTF-8")))
      }
      val allowed = (for {
        host <- Try(new URL(urlString).getHost)
        ruleSet <- Try(rules(host))
      } yield ruleSet.isAllowed(urlString)).getOrElse(true)
      if (allowed)
        push(urlString)
      else
        println("Rejected access by robots.txt to " + urlString)
    }
  }

  def pop(): Future[String] = synchronized {
    val link = queue.head._1
    queue -= link
    Future.successful(link)
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
}
