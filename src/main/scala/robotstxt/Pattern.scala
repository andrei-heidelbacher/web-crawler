package robotstxt

import scala.util.matching.Regex

/**
 * @author andrei
 */
final case class Pattern private (regex: Regex, priority: Int)
  extends Ordered[Pattern] {
  def matches(string: String): Boolean = string match {
    case regex(_*) => true
    case _ => false
  }

  def compare(that: Pattern): Int =
    if (priority != that.priority) -priority.compare(that.priority)
    else regex.toString().compare(that.regex.toString())
}

object Pattern {
  def apply(pattern: String): Pattern = {
    val dollar = if (pattern.last == '$') "$" else ""
    val p = if (dollar == "$") pattern.dropRight(1) else pattern + "*"
    val regex =
      ("\\Q" + p.split("\\*", -1).mkString("\\E.*?\\Q") + "\\E" + dollar).r
    val priority = pattern.length
    Pattern(regex, priority)
  }
}