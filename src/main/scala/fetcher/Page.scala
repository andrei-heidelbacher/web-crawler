package fetcher

import java.net.URL

import scala.util.Try

/**
 * @author andrei
 */
final case class Page(url: URL, content: Array[Byte]) {
  val outlinks: Seq[URL] = {
    val contentAsString = new String(content, "UTF-8")
    val linkRegex = """href="([\w\d\Q-._~:/?#[]@!$&'()*+,;=\E]*+)"""".r
    linkRegex.findAllIn(contentAsString).matchData
      .map(link => link.group(1))
      .toList.flatMap(link => Try(new URL(url, link)).toOption)
  }
}