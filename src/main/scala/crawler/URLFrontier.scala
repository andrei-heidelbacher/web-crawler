package crawler

import scala.collection.mutable

/**
 * @author andrei
 */
final class URLFrontier(initial: Traversable[String]) {
  private val queue = mutable.Queue.empty[String] ++ initial

  def push(url: String): Unit = synchronized(queue.enqueue(url))

  def pop(): String = synchronized(queue.dequeue())

  def isEmpty: Boolean = synchronized(queue.isEmpty)

  def length: Int = synchronized(queue.length)
}

object URLFrontier {
  def apply(initial: String*): URLFrontier =
    new URLFrontier(initial)

  def apply(initial: Traversable[String]): URLFrontier =
    new URLFrontier(initial)
}
