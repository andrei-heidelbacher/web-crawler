package fetcher

import java.net.URL
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
    val url = new URL("https://github.com")
    val page = fetcher.fetch(url)
    for (p <- page) {
      println(p.protocol)
      println(p.host)
      println(p.port)
      println(p.path)
      println(p.URL)
      println(p.content)
      println(p.outlinks)
    }
    Await.result(page, 5.seconds)
  }
}
