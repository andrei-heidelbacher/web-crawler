package robotstxt

/**
 * Set containing all rules in a robots.txt. Supports the `Allow`, `Disallow`,
 * `Crawl-delay` directives. If there are patterns that both explicitly allow
 * and explicitly disallow a path, the one with greater priority is applied
 * (the priority is equal to the length of the originating path pattern from
 * the robots.txt file).
 *
 * @param allowedPaths Set of explicitly allowed patterns
 * @param disallowedPaths Set of explicitly disallowed patterns
 * @param crawlDelay Delay between successive requests in seconds
 *
 * @author andrei
 */
final class RuleSet private (
    allowedPaths: Set[Pattern],
    disallowedPaths: Set[Pattern],
    crawlDelay: Double) {
  /**
   * Checks whether the given path is allowed
   */
  def isAllowed(path: String): Boolean = {
    val allowed = allowedPaths.find(_.matches(path)).map(_.priority)
    val disallowed = disallowedPaths.find(_.matches(path)).map(_.priority)
    allowed.getOrElse(-1) >= disallowed.getOrElse(-1)
  }

  /**
   * Checks whether the given path is disallowed
   */
  def isDisallowed(path: String): Boolean = !isAllowed(path)

  /**
   * @return Crawl-delay in milliseconds or 0 if not specified
   */
  def delayInMs: Int = (1000 * crawlDelay).toInt
}

/**
 * Factory object for the [[RuleSet]] class.
 *
 * @author andrei
 */
object RuleSet {
  /**
   * @param allow Collection of paths mentioned in "Allow" directives
   * @param disallow Collection of paths mentioned in "Disallow" directives
   * @param crawlDelay Real number mentioned in "Crawl-delay" directive
   * @return Corresponding [[RuleSet]]
   */
  def apply(
      allow: Traversable[String],
      disallow: Traversable[String],
      crawlDelay: Double = 0.0): RuleSet = new RuleSet(
    allow.toSet[String].map(p => Pattern(p)),
    disallow.toSet[String].map(p => Pattern(p)),
    Math.max(crawlDelay, 0.0))

  /**
   * @return Empty [[RuleSet]] (equivalent to an empty robots.txt file)
   */
  def empty: RuleSet = RuleSet(Nil, Nil, 0.0)
}