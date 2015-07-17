package fetcher

import dispatch._, Defaults._

import java.net.URL

import scala.util.{Failure, Success, Try}

/**
 * Sends http requests and fetches web pages at given URLs.
 *
 * @param userAgent User-agent string to use when making http requests
 * @param followRedirects Whether the fetcher should follow URL redirects or not
 * @param connectionTimeoutInMs Time in milliseconds to wait for establishing
 * a connection before throwing a timeout exception
 * @param requestTimeoutInMs Time in milliseconds to wait for receiving the http
 * response before throwing a timeout exception
 *
 * @author andrei
 */
final case class PageFetcher(
    userAgent: String,
    followRedirects: Boolean,
    connectionTimeoutInMs: Int,
    requestTimeoutInMs: Int) {
  /**
   * Configured HTTP object that sends the requests.
   */
  private val http = Http.configure {
    _.setFollowRedirects(followRedirects)
      .setConnectionTimeoutInMs(connectionTimeoutInMs)
      .setRequestTimeoutInMs(requestTimeoutInMs)
  }

  /**
   * @param urlString URL to fetch
   * @return Future containing the fetched page or encountered exceptions
   */
  def fetch(urlString: String): Future[Page] = {
    val page = Try {
      val query = url(urlString).addHeader("User-Agent", userAgent)
      http(query OK as.Bytes).map(content => Page(new URL(urlString), content))
    }
    page match {
      case Success(p) => p
      case Failure(t) => Future.failed[Page](t)
    }
  }

  /**
   * @param url URL to fetch
   * @return Future containing the fetched page or encountered exceptions
   */
  def fetch(url: URL): Future[Page] = fetch(url.toString)
}