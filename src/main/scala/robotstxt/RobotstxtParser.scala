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
    val allow = "[aA][lL][lL][oO][wW]"
    val disallow = "[dD][iI][sS]" + allow
    val permissionType = "(" + allow + "|" + disallow + ")"
    val path = "(.*)"
    "(?:\\n" + permissionType + " *: *" + path + ")"
  }

  val permissionRegex: Regex = permissionString.r

  private def userAgentString(userAgent: String): String =
    "[uU][sS][eE][rR]-[aA][gG][eE][nN][tT] *: *" + userAgent

  private val wildcardString = "[\\s\\S]*"

  private def contentString(userAgent: String): String =
    wildcardString +
      userAgentString(userAgent) +
      "(" + permissionString + "*)" +
      wildcardString

  def contentRegex(userAgent: String): Regex =
    contentString(userAgent).r
}