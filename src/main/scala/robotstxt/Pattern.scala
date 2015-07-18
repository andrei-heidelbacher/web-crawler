package robotstxt

import scala.util.matching.Regex

/**
 * Pattern that matches relative URLs in robots.txt files. It supports the
 * wildcard `*` and the end-of-string `$`.
 *
 * @param regex Regular expression that describes the pattern
 * @param priority Priority of the Pattern (equal to the length of the
 * originating string)
 *
 * @author andrei
 */
final case class Pattern private (regex: Regex, priority: Int)
  extends Ordered[Pattern] {
  /**
   * Checks whether the given string matches the pattern.
   */
  def matches(string: String): Boolean = string match {
    case regex(_*) => true
    case _ => false
  }

  /**
   * Compares the priorities of `this` and `that`, and in case of equality,
   * lexicographically compares the associated regexes.
   */
  def compare(that: Pattern): Int =
    if (priority != that.priority) -priority.compare(that.priority)
    else regex.toString().compare(that.regex.toString())
}

/**
 * Factory object for the [[Pattern]] class.
 *
 * @author andrei
 */
object Pattern {
  /**
   * Builds pattern from given string. All characters except `*` and a single
   * `$` at the end of the string are escaped.
   */
  def apply(pattern: String): Pattern = {
    val dollar = if (pattern.last == '$') "$" else ""
    val p = if (dollar == "$") pattern.dropRight(1) else pattern + "*"
    val regex =
      ("\\Q" + p.split("\\*", -1).mkString("\\E.*?\\Q") + "\\E" + dollar).r
    val priority = pattern.length
    Pattern(regex, priority)
  }
}