package crawler

import rx.lang.scala.Subscription

import fetcher.Page

import java.net.URL

import scala.util.Try

/**
 * Interface for observers that process crawled pages. Offers an implemented
 * `run` method to start a crawling process with given seed pages and allows
 * subscribing to crawling processes without dependencies to the RX external
 * library.
 *
 * @author andrei
 */
trait Runner {
  /**
   * Configuration to use for started crawling processes.
   */
  def configuration: CrawlConfiguration

  /**
   * Actions to take on a successfully fetched page or on failed fetches.
   */
  def process(result: (URL, Try[Page])): Unit

  /**
   * Actions to take if the crawling process encounters an error.
   */
  def onError(error: Throwable): Unit

  /**
   * Actions to take if the crawling process terminates successfully (all valid
   * and accessible URLs have been crawled or the crawl limit has been reached).
   */
  def onCompleted(): Unit

  /**
   * Starts a crawling process.
   *
   * @param seeds Pages used as a starting place for crawling
   * @return Subscription that allows cancelling the crawl process by calling
   * the `unsubscribe` method
   */
  final def run(seeds: Traversable[URL]): Subscription = {
    val pageStream = WebCrawler.crawl(configuration, seeds)
    pageStream.subscribe(process, onError, onCompleted)
  }

  /**
   * Starts a crawling process.
   *
   * @param seeds Pages used as a starting place for crawling
   * @return Subscription that allows cancelling the crawl process by calling
   * the `unsubscribe` method
   */
  final def run(seeds: URL*): Subscription = run(seeds.toList)
}
