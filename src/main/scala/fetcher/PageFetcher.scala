package fetcher

import dispatch._, Defaults._

/**
 * @author andrei
 */
class PageFetcher(userAgent: String, followRedirects: Boolean, timeout: Int) {
  def fetch(urlString: String): Future[String] = {
    val query = url(urlString).addHeader("User-Agent", userAgent)
    val http = Http.configure {
      _.setFollowRedirects(true).setConnectionTimeoutInMs(timeout)
    }
    http(query OK as.String)
  }
}

object PageFetcher {
  def apply(
      userAgent: String,
      followerRedirects: Boolean = true,
      timout: Int = 1000): PageFetcher =
    new PageFetcher(userAgent, followerRedirects, timout)
}