package crawler

import fetcher._
import java.io.{File, PrintWriter}
import rx.lang.scala.Observable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success}

/**
 * @author andrei
 */
object WebCrawler {
  def crawl(configuration: CrawlConfiguration)
           (initial: Traversable[String]): Observable[(String, Try[Page])] = {
    Observable[(String, Try[Page])](subscriber => {
      val fetcher = PageFetcher(
        configuration.userAgentString,
        configuration.followRedirects,
        configuration.connectionTimeout,
        configuration.requestTimeout)
      val frontier = URLFrontier(configuration, initial)
      var count = 0
      while (count < 1000 && !subscriber.isUnsubscribed) {
        if (!frontier.isEmpty) {
          val url = frontier.pop()
          val page = fetcher.fetch(url)
          count += 1
          page.onComplete(result => {
            subscriber.onNext(url -> result)
            result match {
              case Success(p) =>
                p.outlinks.foreach(frontier.tryPush)
              case _ => ()
            }
          })
        }
      }
      subscriber.onCompleted()
    })
  }

  def main(args: Array[String]): Unit = {
    val args = Array[String]("history.txt", "http://www.reddit.com/")
    for {
      fileName <- Try(args(0))
      initial <- Try(args.tail)
    } {
      val writer = new PrintWriter(new File(fileName))
      val configuration = CrawlConfiguration(
        "HHbot",
        "HHbot https://github.com/andrei-heidelbacher/web-crawler",
        true,
        3000,
        10000,
        url => true)
      val pageStream = crawl(configuration)(initial)
      val logger = pageStream.subscribe(_ match {
        case (link, Success(page)) =>
          println(link)
          writer.println(link)
          writer.flush()
        case _ => ()
      })
      //writer.close()
      //println("Finished!")
    }
  }
}
