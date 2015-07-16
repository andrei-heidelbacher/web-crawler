package crawler.frontier

import crawler.CrawlConfiguration
import crawler.WebCrawler.executionContext

import java.net.URL
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.util.{Success, Try}

/**
 * @author andrei
 */
final class URLFrontier(
    configuration: CrawlConfiguration,
    seeds: Traversable[URL]) {
  private val robots = RobotstxtSet(configuration)
  private val queue = HostQueue(configuration)
  private val history = URIHistory(configuration)
  private val working = new AtomicLong(0L)

  seeds.foreach(tryPush)

  private def push(url: URL): Unit = synchronized {
    queue.pushURL(url)
    history.add(url)
  }

  def tryPush(url: URL): Unit = {
    if (configuration.URLFilter(url) && !history.contains(url)) {
      working.incrementAndGet()
      robots.isAllowed(url).onComplete {
        case Success(allowed) =>
          if (allowed)
            push(url)
          working.decrementAndGet()
        case _ => working.decrementAndGet()
      }
    }
  }

  def pop(): Future[URL] = synchronized {
    val url = queue.pop()
    robots.delayInMs(url).flatMap(delayInMs => {
      val crawlDelay = configuration.minCrawlDelayInMs.milliseconds
        .max(delayInMs.milliseconds)
      URLFrontier.delay(url, crawlDelay)
    })
  }

  def isEmpty: Boolean = synchronized(queue.isEmpty)

  def isIdle: Boolean = synchronized(working.get == 0L)
}

object URLFrontier {
  def apply(configuration: CrawlConfiguration, seeds: URL*): URLFrontier =
    new URLFrontier(configuration, seeds)

  def apply(
      configuration: CrawlConfiguration,
      seeds: Traversable[URL]): URLFrontier =
    new URLFrontier(configuration, seeds)

  private def never[T]: Future[T] = {
    val p = Promise[T]()
    p.future
  }

  private def delay[T](block: => T, duration: Duration): Future[T] = {
    val p = Promise[T]()
    Future(Await.ready(never, duration)).onComplete(f => p.complete(Try(block)))
    p.future
  }
}
