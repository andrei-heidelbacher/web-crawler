package fetcher

import dispatch._, Defaults._
import scala.util.{Try, Success, Failure}
import java.net.URL

/**
 * @author andrei
 */
final case class PageFetcher(
    userAgent: String,
    followRedirects: Boolean = true,
    connectionTimeout: Int = 3000,
    requestTimeout: Int = 10000) {
  private val http = Http.configure {
    _.setFollowRedirects(followRedirects)
      .setConnectionTimeoutInMs(connectionTimeout)
      .setRequestTimeoutInMs(requestTimeout)
  }

  def fetch(urlString: String): Future[Page] = {
    val page = Try {
      val query = url(urlString).addHeader("User-Agent", userAgent)
      http(query OK as.Bytes).map(content => Page(urlString, content))
    }
    page match {
      case Success(p) => p
      case Failure(t) => Future.failed[Page](t)
    }
  }

  def fetch(url: URL): Future[Page] = fetch(url.toString)
}