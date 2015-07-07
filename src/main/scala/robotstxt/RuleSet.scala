package robotstxt

import scala.util.Try

/**
 * @author andrei
 */
final class RuleSet private (
    allowedPaths: Set[Pattern],
    disallowedPaths: Set[Pattern],
    crawlDelay: Double) {
  def isAllowed(path: String): Boolean = {
    val allowed = allowedPaths.find(_.matches(path)).map(_.priority)
    val disallowed = disallowedPaths.find(_.matches(path)).map(_.priority)
    allowed.getOrElse(-1) >= disallowed.getOrElse(-1)
  }

  def isDisallowed(path: String): Boolean = !isAllowed(path)

  def delay: Double = crawlDelay
}

object RuleSet {
  def apply(
      allow: Traversable[String],
      disallow: Traversable[String],
      crawlDelay: Double = 0.0): RuleSet = new RuleSet(
    allow.toSet.filter(Pattern.valid).map(p => Pattern(p)),
    disallow.toSet.filter(Pattern.valid).map(p => Pattern(p)),
    crawlDelay)

  def apply(
      allow: Traversable[String],
      disallow: Traversable[String],
      crawlDelay: String): RuleSet = {
    val delay = Try(crawlDelay.toDouble)
    RuleSet(allow, disallow, delay.getOrElse(0.0))
  }

  def apply(
      allow: Traversable[String],
      disallow: Traversable[String],
      crawlDelay: Traversable[String]): RuleSet = {
    val delay = Try {
      assert(crawlDelay.size == 1)
      crawlDelay.head.toDouble
    }
    RuleSet(allow, disallow, delay.getOrElse(0.0))
  }
}