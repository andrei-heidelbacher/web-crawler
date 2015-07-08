package crawler

/**
 * @author andrei
 */
final case class UserAgent(agentName: String, userAgentString: String) {
  require(userAgentString.startsWith(agentName))
}
