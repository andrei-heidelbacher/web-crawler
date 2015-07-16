package crawler.frontier

import crawler.CrawlConfiguration

import java.net.{URI, URL}

import scala.collection._
import scala.util.Try

/**
 * @author andrei
 */
final class URIHistory private (maxSize: Int) {
  private val history = mutable.Set.empty[URI]
  private val queue = mutable.Queue.empty[URI]

  def add(url: URL): Unit = synchronized {
    for {
      uri <- Try(url.toURI)
      if !history.contains(uri)
    } {
      while (history.size >= maxSize) {
        history -= queue.dequeue()
      }
      queue.enqueue(uri)
      history += uri
    }
  }

  def contains(url: URL): Boolean = synchronized {
    Try(history.contains(url.toURI)).getOrElse(false)
  }
}

object URIHistory {
  def apply(configuration: CrawlConfiguration): URIHistory =
    new URIHistory(configuration.maxHistorySize)
}