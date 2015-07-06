package fetcher

import java.net.URL

/**
 * @author andrei
 */
class Page(url: URL, val content: Array[Byte]) {
  val URL: String = url.toString

  def host: String = url.getHost

  def path: String = url.getPath

  def port: Int = url.getPort

  def protocol: String = url.getProtocol

  val outlinks: Seq[String] = {
    val contentAsString = new String(content, "UTF-8")
    val linkRegex = """href="([\w\d\Q-._~:/?#[]@!$&'()*+,;=\E]*)"""".r
    linkRegex.findAllIn(contentAsString).matchData.map({ matchedLink =>
      val link = matchedLink.group(1)
      if (!link.startsWith("http"))
        url.toString + (if (link.startsWith("/")) "" else "/") + link
      else
        link
    }).toList
  }
}

object Page {
  def apply(url: URL, content: Array[Byte]): Page =
    new Page(url, content)

  def apply(url: String, content: Array[Byte]): Page =
    new Page(new URL(url), content)
}