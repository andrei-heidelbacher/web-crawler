package fetcher

import org.scalatest._
import scala.concurrent._
import scala.concurrent.duration._

/**
 * @author andrei
 */
class PageFetcherTests extends FunSuite {
  test("Sample requests") {
    val urls = Seq(
      "https://github.com",
      "https://github.com/robots.txt",
      "http://www.infoarena.ro",
      "http://www.infoarena.ro/problema/text")
    val userAgent = "HHbot"
    val fetcher = PageFetcher(userAgent)
    urls.foreach(p =>
      println(Await.result(fetcher.fetch(p), 5.seconds).content))
  }
}
