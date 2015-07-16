package crawler.frontier

import crawler.CrawlConfiguration
import crawler.WebCrawler.executionContext
import fetcher.PageFetcher
import robotstxt.{RobotstxtParser, RuleSet}

import java.net.URL

import scala.collection.concurrent
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * @author andrei
 */
final class RobotstxtSet private (
    agentName: String,
    userAgentString: String,
    followRedirects: Boolean,
    connectionTimeoutInMs: Int,
    requestTimeoutInMs: Int,
    maxRobotsSize: Int,
    maxRobotsHistory: Int) {
  private val fetcher = PageFetcher(
    userAgentString,
    followRedirects,
    connectionTimeoutInMs,
    requestTimeoutInMs)
  private val parser = RobotstxtParser(agentName)
  private val rules = concurrent.TrieMap[String, RuleSet]()

  private def fetchRobotstxt(url: URL): Future[RuleSet] = synchronized {
    rules += url.getHost -> RuleSet.empty
    Future {
      val link = url.getProtocol + "://" + url.getHost + "/robots.txt"
      val robots = Try(Await.result(fetcher.fetch(link), Duration.Inf))
      val byteContent = robots.map(_.content).getOrElse(Array[Byte]())
      val content = new String(byteContent, "UTF-8")
      if (byteContent.length <= maxRobotsSize)
        Try(parser.getRules(content)).getOrElse(RuleSet.empty)
      else
        RuleSet.empty
    }
  }

  private def ruleSet(url: URL): Future[RuleSet] = {
    if (rules.size > maxRobotsHistory)
      rules.clear()
    val host = url.getHost
    if (rules.contains(host))
      Future.successful(rules(host))
    else
      fetchRobotstxt(url).map(ruleSet => {
        rules += (host -> ruleSet)
        ruleSet
      })
  }

  def isAllowed(url: URL): Future[Boolean] =
    ruleSet(url).map(_.isAllowed(url.getPath))

  def delayInMs(url: URL): Future[Int] =
    ruleSet(url).map(_.delayInMs)
}

object RobotstxtSet {
  def apply(configuration: CrawlConfiguration): RobotstxtSet =
    new RobotstxtSet(
      configuration.agentName,
      configuration.userAgentString,
      configuration.followRedirects,
      configuration.connectionTimeoutInMs,
      configuration.requestTimeoutInMs,
      configuration.maxRobotsSize,
      configuration.maxRobotsHistory)
}