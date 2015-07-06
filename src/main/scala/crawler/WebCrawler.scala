package crawler

import fetcher._
import java.io.{FileOutputStream, File, PrintWriter}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

//import scala.async.Async.{async, await}

/**
 * @author andrei
 */
object WebCrawler {
  val userAgent: String = "HHbot"

  def main(args: Array[String]): Unit = {
    val args = Array[String]("history.txt", "http://www.dmoz.org/")
    for {
      fileName <- Try(args(0))
      frontier <- Try(URLFrontier(args.tail))
    } {
      val fetcher = PageFetcher(userAgent)
      val writer = new PrintWriter(new File(fileName))
      var count = 0
      while (!frontier.isEmpty && count < 100) {
        val url = frontier.pop()
        val page = fetcher.fetch(url)
        println(url)
        for (p <- Try(Await.result(page, 5.seconds))) {
          writer.println(p.URL)
          p.outlinks.foreach(frontier.push)
          println(frontier.length)
          if (url.endsWith(".ico")) {
            val image = new FileOutputStream("img.ico")
            image.write(p.content)
            image.close()
          }
          count += 1
        }
        /*page.onComplete {
          case Success(p) =>
            writer.println(p.URL)
            p.outlinks.foreach(frontier.push)
            println(frontier.length)
            count += 1
          case _ =>
            println("Failed!")
        }*/
        //Await.result(page, 5.seconds)
        println(count)
      }
      writer.close()
    }
  }
}
