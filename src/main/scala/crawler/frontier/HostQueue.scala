package crawler.frontier

import java.net.URL

import scala.collection._

/**
 * @author andrei
 */
final class HostQueue {
  private val MaximumHostCount = 10000
  private val MaximumHostSize = 10000

  private val hostURLs = mutable.Map[String, mutable.Queue[URL]]()
  private val hosts = mutable.Queue[String]()

  private def pushHost(host: String): Unit = synchronized {
    while (hosts.size >= MaximumHostCount)
      hostURLs -= hosts.dequeue()
    hostURLs += host -> mutable.Queue[URL]()
    hosts.enqueue(host)
  }

  def pushURL(url: URL): Unit = synchronized {
    if (!hostURLs.contains(url.getHost))
      pushHost(url.getHost)
    val urlQueue = hostURLs.get(url.getHost).get
    while (urlQueue.size >= MaximumHostSize)
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
  def apply(urls: URL*): HostQueue = {
    val queue = empty
    urls.foreach(queue.pushURL)
    queue
  }

  def apply(urls: Traversable[URL]): HostQueue = {
    val queue = empty
    urls.foreach(queue.pushURL)
    queue
  }

  def empty: HostQueue = new HostQueue
}