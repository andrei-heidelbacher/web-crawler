package robotstxt

import scala.util.matching.Regex

/**
 * @author andrei
 */
final class RobotstxtParser(userAgent: String) {
  private def getRulesFromPermissions(content: String): RuleSet = {
    val permissions =
      RobotstxtParser.permissionRegex.findAllIn(content).toSeq.map({
        case RobotstxtParser.permissionRegex(access, path) => (access, path)
      })
    val (allow, disallow) =
      permissions.foldLeft((List[String](), List[String]()))({
        case ((a, d), (access, path)) =>
          if (access.toLowerCase == "allow") (path :: a, d) else (a, path :: d)
      })
    RuleSet(allow, disallow)
  }

  private def getRulesWithUserAgent(
      agentName: String,
      content: String): Option[RuleSet] = RobotstxtParser
    .contentRegex(agentName)
    .unapplySeq(content)
    .flatMap(g => g.headOption)
    .map(getRulesFromPermissions)

  def getRules(content: String): RuleSet =
    getRulesWithUserAgent(userAgent, content)
      .orElse(getRulesWithUserAgent("\\*", content))
      .getOrElse(RuleSet(Nil, Nil))
}

object RobotstxtParser {
  def apply(userAgent: String) = new RobotstxtParser(userAgent)

  private val permissionString: String = {
    val allow = "(?:a|A)(?:l|L)(?:l|L)(?:o|O)(?:w|W)"
    val disallow = "(?:d|D)(?:i|I)(?:s|S)(?:a|A)(?:l|L)(?:l|L)(?:o|O)(?:w|W)"
    val permissionType = "(" + allow + "|" + disallow + ")"
    val path = "(.*)"
    "(?:\\n" + permissionType + ": " + path + ")"
  }

  val permissionRegex: Regex = permissionString.r

  private def userAgentString(userAgent: String): String =
    "(?:u|U)(?:s|S)(?:e|E)(?:r|R)-(?:a|A)(?:g|G)(?:e|E)(?:n|N)(?:t|T): " +
      userAgent

  private def contentString(userAgent: String): String =
    ".*" + userAgentString(userAgent) + "(" + permissionString + "*).*"

  def contentRegex(userAgent: String): Regex =
    contentString(userAgent).r
}