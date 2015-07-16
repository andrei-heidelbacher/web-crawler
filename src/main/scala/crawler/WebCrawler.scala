package crawler

import rx.lang.scala.Observable

import crawler.frontier.URLFrontier
import fetcher._

import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * @author andrei
 */
object WebCrawler {
  implicit val executionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1000))
    //scala.concurrent.ExecutionContext.Implicits.global

  def crawl(
      configuration: CrawlConfiguration,
      seeds: Traversable[URL]): Observable[(URL, Try[Page])] = {
    Observable[(URL, Try[Page])](subscriber => {
      val frontier = URLFrontier(configuration, seeds)
      val fetcher = PageFetcher(
        configuration.userAgentString,
        configuration.followRedirects,
        configuration.connectionTimeoutInMs,
        configuration.requestTimeoutInMs)
      val working = new AtomicLong(0L)
      var crawled = 0L
      while (!subscriber.isUnsubscribed &&
        crawled < configuration.crawlLimit &&
        (working.get > 0L || !frontier.isIdle || !frontier.isEmpty)) {
        if (!frontier.isEmpty) {
          working.incrementAndGet()
          crawled += 1
          val url = frontier.pop()
          val page = url.flatMap(fetcher.fetch)
          page.onComplete(p => {
            subscriber.onNext(Await.result(url, Duration.Inf) -> p)
            p.map(_.outlinks).foreach(_.foreach(frontier.tryPush))
            working.decrementAndGet()
          })
        }
      }
      while (!subscriber.isUnsubscribed && working.get > 0) {}
      subscriber.onCompleted()
    })
  }
}
