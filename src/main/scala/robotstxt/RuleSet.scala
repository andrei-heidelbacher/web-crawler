package robotstxt

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
    allow.toSet[String].map(p => Pattern(p)),
    disallow.toSet[String].map(p => Pattern(p)),
    Math.max(crawlDelay, 0.0))
}