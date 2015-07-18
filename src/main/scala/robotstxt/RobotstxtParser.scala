package robotstxt

import scala.util.Try

/**
 * Parses rules from robots.txt files for given `agentName`. If there is a
 * "User-agent" directive with the given `agentName`, then the directives
 * specified in that group are parsed. Otherwise, the directives associated with
 * all user-agents (wildcard `*`) are parsed.
 *
 * @param agentName User-agent to search in directives
 *
 * @author andrei
 */
final class RobotstxtParser(val agentName: String) {
  /**
   * @param content Substring of the robots.txt file that matched all
   * recognized directives
   * @return [[RuleSet]] parsed from `content`
   */
  private def getRulesFromDirectives(content: String): RuleSet = {
    val directiveRegex = RobotstxtParser.directive.r
    val directives = directiveRegex.findAllIn(content).toSeq.flatMap({
      case directiveRegex(directive, subject) => RobotstxtParser.supported
        .filter(directive.matches)
        .map(d => (d, subject))
    }).filter({ case (directive, subject) => subject.nonEmpty })
      .groupBy({ case (directive, subject) => directive })
      .mapValues(_.map({ case (k, v) => v }))
    val allow = directives.getOrElse(RobotstxtParser.allow, Nil)
    val disallow = directives.getOrElse(RobotstxtParser.disallow, Nil)
    val crawlDelay = Try {
      directives(RobotstxtParser.crawlDelay).map(_.toDouble).min
    }
    RuleSet(allow, disallow, crawlDelay.getOrElse(0.0))
  }

  /**
   * @param userAgent User-agent of the crawler to search in the robots.txt file
   * @param content Sanitized content of the robots.txt file (without comments)
   * @return Parsed [[RuleSet]] from `content`
   */
  private def getRulesForUserAgent(
      userAgent: String,
      content: String): Option[RuleSet] = RobotstxtParser
    .content(userAgent).r
    .unapplySeq(content)
    .flatMap(g => g.headOption)
    .map(getRulesFromDirectives)

  /**
   * @param rawContent Content of the robots.txt file converted to a UTF-8
   * encoded string from a byte array
   * @return Parsed [[RuleSet]] from given robots.txt
   */
  def getRules(rawContent: String): RuleSet = {
    val content = rawContent.replaceAll(RobotstxtParser.comment, "")
    getRulesForUserAgent(agentName, content)
      .orElse(getRulesForUserAgent("\\*", content))
      .getOrElse(RuleSet.empty)
  }
}

/**
 * Factory object for [[RobotstxtParser]] class.
 *
 * @author andrei
 */
object RobotstxtParser {
  /**
   * Builds a parser with given `agentName`.
   */
  def apply(agentName: String) = new RobotstxtParser(agentName)

  val comment = """[\s ]#.*+"""
  val wildcard = """[\s\S]*"""
  val value = """([\w\Q-.~:/?#[]@!$&'()*+,;=\E]*+)"""
  val userAgent = """[uU][sS][eE][rR]-[aA][gG][eE][nN][tT]"""
  val allow = """[aA][lL][lL][oO][wW]"""
  val disallow = """[dD][iI][sS]""" + allow
  val crawlDelay = """[cC][rR][aA][wW][lL]-[dD][eE][lL][aA][yY]"""
  val supported = Seq(allow, disallow, crawlDelay, "[^: ]*+")
  val directive =
    "(?:\\s(" + supported.mkString("|") + ") *+: *+" + value + " *+)"

  /**
   * Regex used to match the "User-agent" directive.
   */
  def userAgentDirective(agentName: String): String =
    userAgent + " *+: *+" + agentName + " *+"

  /**
   * Regex used to match robots.txt content.
   */
  def content(agentName: String): String =
    wildcard +
      userAgentDirective(agentName) + "(" + directive + "*+)" +
      wildcard
}