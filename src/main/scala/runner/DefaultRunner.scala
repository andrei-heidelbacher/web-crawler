package runner

import crawler._
import fetcher._

import java.io.PrintWriter
import java.net.URL

import scala.util.{Failure, Success, Try}

/**
 * @author andrei
 */
object DefaultRunner extends Runner {
  private val writer = new PrintWriter("history.log")

  def configuration: CrawlConfiguration = CrawlConfiguration(
    "HHbot",
    "HHbot https://github.com/andrei-heidelbacher/web-crawler",
    0 == 0,
    5000,
    20000,
    500,
    url => {
      !url.toString.matches(".*\\.(css|png|jpg|pdf|json|ico)")
    },
    1024 * 256,
    10000,
    10000,
    1000,
    1000000,
    10000)

  def process(result: (URL, Try[Page])): Unit = {
    result match {
      case (link, Success(page)) =>
        println("Successfully fetched " + link)
        writer.println(link)
        writer.flush()
      case (link, Failure(t)) =>
        println("Failed to fetch " + link + " because " + t.getMessage)
    }
  }

  def onError(error: Throwable): Unit = {
    println("Encountered an error: " + error.getMessage)
  }

  def onCompleted(): Unit = {
    writer.close()
    println("Finished!")
  }

  def main(args: Array[String]): Unit = {
    val args = Array[String](
      "http://www.gsp.ro",
      "http://www.reddit.com",
      "http://www.wikipedia.org",
      "http://www.twitter.com")
    for (initial <- Try(args.tail.map(new URL(_)))) {
      run(initial)
    }
  }
}
