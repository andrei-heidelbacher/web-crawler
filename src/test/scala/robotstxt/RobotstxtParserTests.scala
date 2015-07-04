package robotstxt

import org.scalatest._

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
    val requests = Seq("/problema/text2", "/problema/text", "/forum/topic")
    //val requests = Seq("/problema/text/teste")
    requests.foreach(p => println(rules.isAllowed(p)))
  }
}
