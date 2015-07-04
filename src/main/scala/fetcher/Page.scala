package fetcher

import java.net.URL

/**
 * @author andrei
 */
class Page(url: URL, val content: String) {
  val URL: String = url.toString

  val host: String = url.getHost

  val path: String = url.getPath

  val port: Int = url.getPort

  val protocol: String = url.getProtocol

  val outlinks: Seq[String] = {
    val linkRegex = """href="([\w\d\Q-._~:/?#[]@!$&'()*+,;=\E]*)"""".r
    linkRegex.findAllIn(content).matchData.map({ matchedLink =>
      val link = matchedLink.group(1)
      if (!link.startsWith("http"))
        url.toString + (if (link.startsWith("/")) "" else "/") + link
      else
        link
    }).toList
  }
}

object Page {
  def apply(url: URL, content: String): Page =
    new Page(url, content)

  def apply(url: String, content: String): Page =
    new Page(new URL(url), content)
}