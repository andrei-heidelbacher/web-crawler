package Runner

import crawler._
import fetcher._

import java.io.{File, PrintWriter}
import java.net.URL

import scala.util.{Failure, Success, Try}

/**
 * @author andrei
 */
object DefaultRunner extends Runner {
  var writer: PrintWriter = new PrintWriter("history.txt")

  def configuration: CrawlConfiguration = CrawlConfiguration(
    "HHbot",
    "HHbot https://github.com/andrei-heidelbacher/web-crawler",
    0 == 0,
    5000,
    20000,
    500,
    url => true,
    1024 * 256,
    "logs.log",
    500)

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
    val args = Array[String]("history.txt", "http://www.reddit.com/")
    for {
      fileName <- Try(args(0))
      initial <- Try(args.tail.map(new URL(_)))
    } {
      writer = new PrintWriter(new File(fileName))
      run(initial)
    }
  }
}
