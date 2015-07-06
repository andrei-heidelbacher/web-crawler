package robotstxt

/**
 * @author andrei
 */
final class RobotstxtParser(agentName: String) {
  private def getRulesFromDirectives(content: String): RuleSet = {
    val directiveRegex = RobotstxtParser.directive.r
    val directives = directiveRegex.findAllIn(content).toSeq.flatMap({
      case directiveRegex(directive, subject) => RobotstxtParser.supported
        .filter(directive.matches)
        .map(d => (d, subject))
    }).groupBy({ case (directive, subject) => directive })
      .mapValues(_.map({ case (k, v) => v }))
    val allow = directives.getOrElse(RobotstxtParser.allow, Nil)
    val disallow = directives.getOrElse(RobotstxtParser.disallow, Nil)
    val crawlDelay = directives.getOrElse(RobotstxtParser.crawlDelay, Nil)
    RuleSet(allow, disallow, crawlDelay)
  }

  private def getRulesWithUserAgent(
      agentName: String,
      content: String): Option[RuleSet] = RobotstxtParser
    .content(agentName).r
    .unapplySeq(content)
    .flatMap(g => g.headOption)
    .map(getRulesFromDirectives)

  def getRules(rawContent: String): RuleSet = {
    val content = rawContent.replaceAll("[\\s ]#.*", "")
    getRulesWithUserAgent(agentName, content)
      .orElse(getRulesWithUserAgent("\\*", content))
      .getOrElse(RuleSet(Nil, Nil, Nil))
  }
}

object RobotstxtParser {
  def apply(userAgent: String) = new RobotstxtParser(userAgent)

  val wildcard = """[\s\S]*"""
  val link = """([\w\d\Q-._~:/?#[]@!$&'()*+,;=\E]*)"""
  val userAgent = """[uU][sS][eE][rR]-[aA][gG][eE][nN][tT]"""
  val allow = """[aA][lL][lL][oO][wW]"""
  val disallow = """[dD][iI][sS]""" + allow
  val crawlDelay = """[cC][rR][aA][wW][lL]-[dD][eE][lL][aA][yY]"""
  val supported = Seq(allow, disallow, crawlDelay, ".*")
  val directive = "(?:\\s(" + supported.mkString("|") + ") *: *" + link + " *)"

  private def userAgentString(agentName: String): String =
    userAgent + " *: *" + agentName + " *"

  private def content(agentName: String): String =
    wildcard + userAgentString(agentName) + "(" + directive + "*)" + wildcard
}