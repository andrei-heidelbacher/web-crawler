package crawler.frontier

import crawler.CrawlConfiguration

import java.net.URL

import scala.collection._

/**
 * @author andrei
 */
final class HostQueue private (maxBreadth: Int, maxDepth: Int) {
  private val hostURLs = mutable.Map.empty[String, mutable.Queue[URL]]
  private val hosts = mutable.Queue.empty[String]

  private def pushHost(host: String): Unit = synchronized {
    while (hosts.size >= maxBreadth)
      hostURLs -= hosts.dequeue()
    hostURLs += host -> mutable.Queue.empty[URL]
    hosts.enqueue(host)
  }

  def pushURL(url: URL): Unit = synchronized {
    if (!hostURLs.contains(url.getHost))
      pushHost(url.getHost)
    val urlQueue = hostURLs.get(url.getHost).get
    while (urlQueue.size >= maxDepth)
      urlQueue.dequeue()
    urlQueue.enqueue(url)
  }

  def pop(): URL = synchronized {
    val host = hosts.dequeue()
    val urlQueue = hostURLs.get(host).get
    val url = urlQueue.dequeue()
    if (urlQueue.isEmpty)
      hostURLs -= host
    else
      hosts.enqueue(host)
    url
  }

  def isEmpty: Boolean = synchronized(hosts.isEmpty)
}

object HostQueue {
  def apply(configuration: CrawlConfiguration): HostQueue =
    new HostQueue(configuration.maxHostBreadth, configuration.maxHostDepth)
}