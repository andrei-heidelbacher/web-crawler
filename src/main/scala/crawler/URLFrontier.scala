package crawler

import java.util.concurrent.ConcurrentLinkedQueue

import fetcher._
import robotstxt._

import java.io.PrintWriter
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

import scala.collection.concurrent
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
  private final class Logger(fileName: String) {
    private val printer = new PrintWriter(fileName)

    private def timeNow: String = {
      import java.util.Calendar

      val now = Calendar.getInstance
      val year = now.get(Calendar.YEAR)
      val month = now.get(Calendar.MONTH) + 1
      val day = now.get(Calendar.DAY_OF_MONTH)
      val hour = now.get(Calendar.HOUR_OF_DAY)
      val minute = now.get(Calendar.MINUTE)
      val second = now.get(Calendar.SECOND)
      f"$year%d-$month%02d-$day%02d $hour%02d:$minute%02d:$second%02d"
    }

    def log(message: String) = synchronized {
      printer.println(timeNow + ": " + message)
      printer.flush()
    }
  }

  private val MaximumHistorySize = 1000000
  private val MaximumHostQueueSize = 100000

  private val fetcher = PageFetcher(
    configuration.userAgentString,
    configuration.followRedirects,
    configuration.connectionTimeoutInMs,
    configuration.requestTimeoutInMs)
  private val parser = RobotstxtParser(configuration.agentName)
  private val rules = concurrent.TrieMap[String, RuleSet]()
  private val hostURLs =
    concurrent.TrieMap[String, collection.mutable.Queue[URL]]()
  private val hostQueue = new ConcurrentLinkedQueue[String]()
  private val history = concurrent.TrieMap[URL, Unit]()
  private val working = new AtomicLong(0L)
  private val logger = new Logger(configuration.logFileName)

  initial.foreach(tryPush)

  private def pushHost(host: String): Unit = {
    hostURLs += host -> collection.mutable.Queue[URL]()
    hostQueue.add(host)
    while (hostQueue.size >= MaximumHostQueueSize)
      hostQueue.remove()
  }

  private def push(url: URL): Unit = synchronized {
    while (history.size >= MaximumHistorySize)
      history -= history.head._1
    history += url -> Unit
    if (!hostURLs.contains(url.getHost))
      pushHost(url.getHost)
    hostURLs.get(url.getHost).get.enqueue(url)
    logger.log("Enqueued " + url)
  }

  private def fetchRobotstxt(url: URL): Future[RuleSet] = Future {
    val link = url.getProtocol + "://" + url.getHost + "/robots.txt"
    val robots = Try(Await.result(fetcher.fetch(link), Duration.Inf))
    val byteContent = robots.map(_.content).getOrElse(Array[Byte]())
    val content = new String(byteContent, "UTF-8")
    if (byteContent.length <= configuration.maxRobotsSize)
      Try(parser.getRules(content)).getOrElse(RuleSet.empty)
    else
      RuleSet.empty
  }

  def isAllowed(url: URL): Future[Boolean] = Future {
    if (!configuration.urlFilter(url) || history.contains(url))
      false
    else {
      if (!rules.contains(url.getHost)) {
        fetchRobotstxt(url).onComplete {
          case Success(ruleSet) =>
            rules += (url.getHost -> ruleSet)
            logger.log("Got robots for " + url.getHost)
          case _ => ()
        }
      }
      (for {
        ruleSet <- Try(rules(url.getHost))
        allowed <- Try(ruleSet.isAllowed(url.getPath))
      } yield allowed).getOrElse(true)
    }
  }

  def tryPush(url: URL): Unit = {
    working.incrementAndGet()
    isAllowed(url).onComplete {
      case Success(allowed) =>
        if (allowed)
          push(url)
        else
          logger.log("Access denied by REP or previously visited " + url)
        working.decrementAndGet()
      case _ => working.decrementAndGet()
    }
  }

  def pop(): Future[URL] = synchronized {
    val host = hostQueue.remove()
    val urlQueue = hostURLs.get(host).get
    val url = urlQueue.dequeue()
    if (urlQueue.isEmpty)
      hostURLs -= host
    logger.log("Dequeued " + url)
    val crawlDelay = configuration.minCrawlDelayInMs.milliseconds
      .max(rules.getOrElse(host, RuleSet.empty).delayInMs.milliseconds)
    URLFrontier.delay(url, crawlDelay)
  }

  def isEmpty: Boolean = hostQueue.isEmpty

  def isIdle: Boolean = working.get == 0L
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
