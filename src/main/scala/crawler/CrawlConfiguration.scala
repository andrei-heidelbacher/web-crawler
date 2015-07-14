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
    urlFilter: URL => Boolean,
    maxRobotsSize: Long,
    logFileName: String,
    crawlLimit: Long) {
  require(userAgentString.startsWith(agentName))
  require(connectionTimeoutInMs > 0)
  require(requestTimeoutInMs > 0)
  require(minCrawlDelayInMs > 0)
  require(maxRobotsSize > 0)
}
