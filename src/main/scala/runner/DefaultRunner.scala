package runner

import crawler._
import fetcher._

import java.io.PrintWriter
import java.net.URL

import scala.util.{Failure, Success, Try}

/**
 * Default implementation for the [[crawler.Runner]] interface. Logs the
 * successfully fetched pages in `history.log` and prints to the standard
 * output all fetch attempts (successful or failed).
 *
 * @author andrei
 */
object DefaultRunner extends Runner {
  private val writer = new PrintWriter("history.log")

  def configuration: CrawlConfiguration = CrawlConfiguration(
    agentName = "HHbot",
    userAgentString = "HHbot" +
      " (https://github.com/andrei-heidelbacher/web-crawler)",
    followRedirects = true,
    connectionTimeoutInMs = 5000,
    requestTimeoutInMs = 20000,
    minCrawlDelayInMs = 500,
    URLFilter = url => {
      !url.toString.matches(".*\\.(css|png|jpg|pdf|json|ico)")
    },
    maxRobotsSize = 1024 * 256,
    maxRobotsHistory = 10000,
    maxHostBreadth = 10000,
    maxHostDepth = 1000,
    maxHistorySize = 1000000,
    crawlLimit = 10000L)

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
    for (initial <- Try(args.map(new URL(_)))) {
      run(initial)
    }
  }
}
