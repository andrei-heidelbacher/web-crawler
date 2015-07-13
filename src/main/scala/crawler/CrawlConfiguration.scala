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
    urlFilter: URL => Boolean,
    crawlLimit: Long,
    maxRobotsSize: Long) {
  require(userAgentString.startsWith(agentName))
  require(connectionTimeoutInMs > 0)
  require(requestTimeoutInMs > 0)
  require(maxRobotsSize > 0)
}
