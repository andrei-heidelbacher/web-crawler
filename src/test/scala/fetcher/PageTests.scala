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
    val follow = true
    val userAgent = "HHbot"
    val fetcher = PageFetcher(userAgent, follow, 3000, 10000)
    val url = "https://github.com"
    val page = fetcher.fetch(url)
    for (p <- page) {
      println(p.url.toString)
      println(p.content)
      println(p.outlinks)
    }
    Await.result(page, 5.seconds)
  }
}
