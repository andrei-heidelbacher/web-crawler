package crawler.frontier

import crawler.CrawlConfiguration
import fetcher.PageFetcher
import robotstxt.{RobotstxtParser, RuleSet}

import java.net.URL

import scala.collection.concurrent
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * @author andrei
 */
final class RobotstxtSet(configuration: CrawlConfiguration) {
  private val fetcher = PageFetcher(
    configuration.userAgentString,
    configuration.followRedirects,
    configuration.connectionTimeoutInMs,
    configuration.requestTimeoutInMs)
  private val parser = RobotstxtParser(configuration.agentName)
  private val rules = concurrent.TrieMap[String, RuleSet]()

  private def fetchRobotstxt(url: URL): Future[RuleSet] = Future {
    val link = url.getProtocol + "://" + url.getHost + "/robots.txt"
    val robots = Try(Await.result(fetcher.fetch(link), Duration.Inf))
    val byteContent = robots.map(_.content).getOrElse(Array[Byte]())
    val content = new String(byteContent, "UTF-8")
    if (byteContent.length <= configuration.maxRobotsSize)
      Try(parser.getRules(content)).getOrElse(RuleSet.empty)
    else
      RuleSet.empty
  }

  private def ruleSet(url: URL): Future[RuleSet] = {
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