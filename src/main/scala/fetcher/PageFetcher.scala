package fetcher

import dispatch._, Defaults._
import java.net.URL

/**
 * @author andrei
 */
final case class PageFetcher(
    userAgent: String,
    followRedirects: Boolean = true,
    connectionTimeout: Int = 3000,
    requestTimeout: Int = 10000) {
  def fetch(urlString: String): Future[Page] = {
    val query = url(urlString).addHeader("User-Agent", userAgent)
    val http = Http.configure {
      _.setFollowRedirects(followRedirects)
        .setConnectionTimeoutInMs(connectionTimeout)
        .setRequestTimeoutInMs(requestTimeout)
    }
    http(query OK as.Bytes).map(content => Page(urlString, content))
  }

  def fetch(url: URL): Future[Page] = fetch(url.toString)
}