package crawler

import rx.lang.scala.Observable

import fetcher._

import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * @author andrei
 */
object WebCrawler {
  def crawl(configuration: CrawlConfiguration)
           (initial: Traversable[URL]): Observable[(URL, Try[Page])] = {
    Observable[(URL, Try[Page])](subscriber => {
      val fetcher = PageFetcher(
        configuration.userAgentString,
        configuration.followRedirects,
        configuration.connectionTimeoutInMs,
        configuration.requestTimeoutInMs)
      val frontier = URLFrontier(configuration, initial)
      val processing = new AtomicInteger(0)
      var crawled = 0L
      while (!subscriber.isUnsubscribed &&
        crawled < configuration.crawlLimit) {
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
}
