package crawler.frontier

import crawler.CrawlConfiguration

import java.net.URL
import java.util.concurrent.atomic.AtomicLong

//import scala.collection.concurrent
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

/**
 * @author andrei
 */
final class URLFrontier(
    configuration: CrawlConfiguration,
    initial: Traversable[URL]) {
  //private val MaximumHistorySize = 10000000

  //private val history = concurrent.TrieMap[URL, Unit]()
  private val robots = new RobotstxtSet(configuration)
  private val queue = HostQueue.empty
  private val working = new AtomicLong(0L)
  private val logger = new Logger(configuration.logFileName)

  initial.foreach(tryPush)

  private def push(url: URL): Unit = synchronized {
    /*while (history.size >= MaximumHistorySize)
      history -= history.head._1
    history += url -> Unit*/
    queue.pushURL(url)
    logger.log("Enqueued " + url)
  }

  def tryPush(url: URL): Unit = {
    if (configuration.urlFilter(url)) {
      working.incrementAndGet()
      robots.isAllowed(url).onComplete {
        case Success(allowed) =>
          if (allowed)
            push(url)
          else
            logger.log("Access denied to " + url)
          working.decrementAndGet()
        case _ => working.decrementAndGet()
      }
    }
  }

  def pop(): Future[URL] = synchronized {
    val url = queue.pop()
    logger.log("Dequeued " + url)
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
  def apply(configuration: CrawlConfiguration, initial: URL*): URLFrontier =
    new URLFrontier(configuration, initial)

  def apply(
      configuration: CrawlConfiguration,
      initial: Traversable[URL]): URLFrontier =
    new URLFrontier(configuration, initial)

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
