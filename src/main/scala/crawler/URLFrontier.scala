package crawler

import scala.collection.mutable
/*import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try
import java.net.URL
import robotstxt._
import fetcher._*/

/**
 * @author andrei
 */
final class URLFrontier(
    configuration: CrawlConfiguration,
    initial: Traversable[String]) {
  private val maximumSize = 500
  /*private val fetcher = PageFetcher(
    configuration.userAgentString,
    configuration.followRedirects,
    configuration.connectionTimeout,
    configuration.requestTimeout)
  private val robotsParser = RobotstxtParser(configuration.agentName)
  private val rules = mutable.Map[String, RuleSet]()*/
  private val queue = mutable.Set.empty[String]

  initial.filter(configuration.urlFilter).foreach(tryPush)

  def push(url: String): Unit = synchronized {
    /*val host = Try(new URL(url).getHost)
    for {
      h <- host
      if !rules.contains(h)
      link = h + "/robots.txt"
      robots <- Try(Await.result(fetcher.fetch(link), Duration.Inf))
    } {
      rules += (h -> robotsParser.getRules(new String(robots.content, "UTF-8")))
    }
    val allowed = for {
      h <- host
      r <- Try(rules(h))
    } yield r.isAllowed(h)
    if (allowed.getOrElse(true))*/
      queue += url
    //else println("Rejected " + url)
  }

  def tryPush(url: String): Unit = synchronized {
    while (queue.size >= maximumSize)
      queue -= queue.head
    if (configuration.urlFilter(url))
      queue += url
  }

  def pop(): String = synchronized {
    val link = queue.head
    queue -= link
    link
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
