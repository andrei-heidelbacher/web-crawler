package robotstxt

import org.scalatest._

import scala.io.Source

/**
 * @author andrei
 */
class RobotstxtParserTests extends FunSuite {
  test("getRules simple test") {
    val parser = RobotstxtParser("HHbot")
    val rules = parser.getRules(
      "User-agent: *\nDisAllow: /problema/car\nDisAllow: " +
        "/problema/text$\nAllow: " +
        "/forum/*")
    val allowed = Seq("/problema/text2", "/forum/topic")
    val disallowed = Seq("/problema/text")
    assert(allowed.forall(rules.isAllowed))
    assert(disallowed.forall(rules.isDisallowed))
  }

  test("GitHub robots.txt test") {
    val stream = getClass.getResourceAsStream("/robots.txt")
    val content = Source.fromInputStream(stream).getLines().mkString("\n")
    val parser = RobotstxtParser("HHbot")
    val rules = parser.getRules(content)
    val allowed = Seq(
      "/andrei-heidelbacher/web-crawler/blob/master/README.md",
      "/andrei-heidelbacher/web-crawler/blob/master/build.sbt"
    )
    val disallowed = Seq(
      "/search",
      "/search/login",
      "/andrei-heidelbacher/web-crawler/graphs/contributors"
    )
    assert(allowed.forall(rules.isAllowed))
    assert(disallowed.forall(rules.isDisallowed))
  }
}
