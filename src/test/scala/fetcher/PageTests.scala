package fetcher

import org.scalatest._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author andrei
 */
class PageTests extends FunSuite {
  test("Sample page") {
    val userAgent = "HHbot"
    val fetcher = PageFetcher(userAgent)
    val url = "https://github.com"
    val page = fetcher.fetch(url)
    for (p <- page) {
      println(p.url)
      println(p.content)
      println(p.outlinks)
    }
    Await.result(page, 5.seconds)
  }
}
