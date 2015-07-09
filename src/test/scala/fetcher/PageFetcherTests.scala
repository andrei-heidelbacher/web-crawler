package fetcher

import org.scalatest._
import scala.concurrent._
import scala.concurrent.duration._

/**
 * @author andrei
 */
class PageFetcherTests extends FunSuite {
  test("Sample requests") {
    val follow = true
    val urls = Seq(
      "https://github.com",
      "https://github.com/robots.txt",
      "http://www.infoarena.ro",
      "http://www.infoarena.ro/problema/text")
    val userAgent = "HHbot"
    val fetcher = PageFetcher(userAgent, follow, 3000, 10000)
    urls.foreach(p =>
      println(Await.result(fetcher.fetch(p), 5.seconds).content))
  }
}
