package crawler

import java.net.URL

/**
 * Contains all information required to fully configure a crawling process.
 *
 * @param agentName Name of the web crawler
 * @param userAgentString User-agent string to use when sending HTTP requests.
 * Must have '''agentName''' as a prefix.
 * @param followRedirects Whether the crawler should follow URL redirects or not
 * @param connectionTimeoutInMs Timeout limit in milliseconds for establishing a
 * connection
 * @param requestTimeoutInMs Timeout limit in milliseconds for receiving the
 * request response
 * @param minCrawlDelayInMs Minimum delay in milliseconds between successive
 * requests at the same host (recommended to be at least 250-500 milliseconds)
 * @param URLFilter Function that filters URLs. Only URLs that give a '''true'''
 * result are crawled.
 * @param maxRobotsSize Maximum size in bytes of robots.txt files that are saved
 * @param maxRobotsHistory Maximum number of robots.txt files to keep in memory
 * at a given point in time
 * @param maxHostBreadth Maximum number of hosts to be placed in the
 * [[frontier.URLFrontier]] at a given point in time
 * @param maxHostDepth Maximum number of URLs belonging to the same host to be
 * placed in the [[frontier.URLFrontier]] at a given point in time
 * @param maxHistorySize Maximum number of visited URLs to keep in memory at a
 * given point in time. If a URL exists in history, it will not be re-crawled.
 * @param crawlLimit Maximum number of pages to crawl (however, they may result
 * in either a successfully fetched [[fetcher.Page]] or in an exception)
 *
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
