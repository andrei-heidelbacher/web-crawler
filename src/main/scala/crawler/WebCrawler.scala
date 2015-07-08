package crawler

import fetcher._
import java.io.{File, PrintWriter}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success}

//import scala.async.Async.{async, await}
//import scala.concurrent.Await
//import scala.concurrent.duration._

/**
 * @author andrei
 */
object WebCrawler {
  val agentName: String = "HHbot"
  val userAgentString: String = agentName +
    " https://github.com/andrei-heidelbacher/web-crawler"

  def main(args: Array[String]): Unit = {
    val args = Array[String]("history.txt", "http://www.dmoz.org/")
    for {
      fileName <- Try(args(0))
      frontier <- Try(URLFrontier(args.tail))
    } {
      val fetcher = PageFetcher(userAgentString)
      val writer = new PrintWriter(new File(fileName))
      var count = 0
      var success = 0
      while (count < 1000) {
        if (!frontier.isEmpty) {
          val url = frontier.pop()
          val page = fetcher.fetch(url)
          count += 1
          println(count + ": " + url)
          page.onComplete {
            case Success(p) =>
              println(p.url + " success!")
              p.outlinks.foreach(frontier.push)
              writer.println(p.url)
              writer.flush()
              success += 1
            case _ =>
              println("Failed!")
          }
        }
      }
      writer.close()
      println("Finished! Success: " + success)
    }
  }
}
