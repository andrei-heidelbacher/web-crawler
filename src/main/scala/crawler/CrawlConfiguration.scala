package crawler

/**
 * @author andrei
 */
final case class CrawlConfiguration(
    agentName: String,
    userAgentString: String,
    followRedirects: Boolean,
    connectionTimeout: Int,
    requestTimeout: Int,
    urlFilter: String => Boolean,
    crawlLimit: Long,
    maxRobotsSize: Long) {
  require(userAgentString.startsWith(agentName))
  require(connectionTimeout > 0)
  require(requestTimeout > 0)
  require(maxRobotsSize > 0)
}
