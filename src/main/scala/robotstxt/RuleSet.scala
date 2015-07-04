package robotstxt

import scala.util.matching.Regex

/**
 * @author andrei
 */
final class RuleSet private (
    allowedPaths: Set[Regex],
    dissalowedPaths: Set[Regex]) {
  def isAllowed(path: String): Boolean =
    allowedPaths.exists(RuleSet.matchPattern(_, path)) ||
      dissalowedPaths.forall(!RuleSet.matchPattern(_, path))

  def isDisallowed(path: String): Boolean = !isAllowed(path)
}

object RuleSet {
  def apply(
      allow: Traversable[String],
      dissalow: Traversable[String]): RuleSet = new RuleSet(
    allow.toSet.filter(validPattern).map(formatPattern),
    dissalow.toSet.filter(validPattern).map(formatPattern))

  def validPattern(pattern: String): Boolean = pattern.nonEmpty

  def formatPattern(pattern: String): Regex = {
    val dollar = if (pattern.last == '$') "$" else ""
    val p = if (dollar == "$") pattern.dropRight(1) else pattern + "*"
    ("\\Q" + p.split("\\*", -1).mkString("\\E.*\\Q") + "\\E" + dollar).r
  }

  def matchPattern(pattern: Regex, string: String): Boolean = string match {
    case pattern(_*) => true
    case _ => false
  }
}