package robotstxt

import org.scalatest._

/**
 * @author andrei
 */
class RuleSetTests extends FunSuite {
  test("RuleSet simple test") {
    val patterns = Seq("/*/problem?user=*&score=100", "/asd", "abc")
    val disallowed = Seq("/asd", "abc",
      "/infoarena/problem?user=a_h1926&score=100")
    val allowed = Seq("was", "k", "/infoarena/problem?user=a_h1926&score=90")
    val rules = RuleSet(Nil, patterns)
    assert(disallowed.forall(p => rules.isDisallowed(p)))
    assert(allowed.forall(p => rules.isAllowed(p)))
    assert(rules.delayInMs == 0)
  }
}
