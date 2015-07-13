package crawler

import rx.lang.scala.Subscription

import fetcher.Page

import java.net.URL

import scala.util.Try

/**
 * @author andrei
 */
trait Runner {
  def configuration: CrawlConfiguration

  def process(result: (URL, Try[Page])): Unit

  def onError(error: Throwable): Unit

  def onCompleted(): Unit

  final def run(initial: Traversable[URL]): Subscription = {
    val pageStream = WebCrawler.crawl(configuration)(initial)
    pageStream.subscribe(process, onError, onCompleted)
  }

  final def run(initial: URL*): Subscription = run(initial.toList)
}
