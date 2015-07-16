package crawler

import java.net.URL

/**
 * @author andrei
 */
final case class CrawlConfiguration(
    agentName: String,
    userAgentString: String,
    followRedirects: Boolean,
    connectionTimeoutInMs: Int,
    requestTimeoutInMs: Int,
    minCrawlDelayInMs: Int,
    URLFilter: URL => Boolean,
    maxRobotsSize: Int,
    maxRobotsHistory: Int,
    maxHostBreadth: Int,
    maxHostDepth: Int,
    maxHistorySize: Int,
    crawlLimit: Long) {
  require(userAgentString.startsWith(agentName))
  require(connectionTimeoutInMs > 0)
  require(requestTimeoutInMs > 0)
  require(minCrawlDelayInMs > 0)
  require(maxRobotsSize > 0)
  require(maxRobotsHistory > 0)
  require(maxHostBreadth > 0)
  require (maxHostDepth > 0)
  require(maxHistorySize > 0)
  require(crawlLimit > 0)
}
