package crawler

import fetcher._
import java.io.{File, PrintWriter}
import java.util.concurrent.atomic.AtomicInteger
import rx.lang.scala.{Observer, Observable}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

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
      val processing = new AtomicInteger(0)
      var crawled = 0L
      while (!subscriber.isUnsubscribed &&
        crawled < configuration.crawlLimit &&
        (processing.get > 0 || !frontier.isEmpty)) {
        if (!frontier.isEmpty) {
          crawled += 1
          processing.incrementAndGet()
          val url = frontier.pop()
          val page = url.flatMap(fetcher.fetch)
          page.onComplete(p => {
            subscriber.onNext(Await.result(url, Duration.Inf) -> p)
            p.map(_.outlinks).foreach(_.foreach(frontier.tryPush))
            processing.decrementAndGet()
          })
        }
      }
      while (!subscriber.isUnsubscribed && processing.get > 0) {}
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
        0 == 0,
        5000,
        20000,
        url => true,
        1000)
      val pageStream = crawl(configuration)(initial)

      val onNext: ((String, Try[Page])) => Unit = { result =>
        result match {
          case (link, Success(page)) =>
            println("Successfully fetched " + link)
            writer.println(link)
            writer.flush()
          case (link, Failure(t)) =>
            println("Failed to fetch " + link + " because " + t.getMessage)
        }
      }

      val onCompleted: () => Unit = { () =>
        writer.close()
        println("Finished!")
      }

      val logger = Observer[(String, Try[Page])](onNext, onCompleted)
      pageStream.subscribe(logger)
    }
  }
}
