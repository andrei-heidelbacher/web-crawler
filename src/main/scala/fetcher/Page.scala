package fetcher

import java.net.URL

import scala.util.Try

/**
 * Web page at given URL with content saved as byte array to allow processing of
 * any file format (images, videos, text etc.). In case the page is a text file,
 * it also parses and saves the outlinks to other pages.
 *
 * @param url URL source of the page
 * @param content Page content as a byte array
 *
 * @author andrei
 */
final case class Page(url: URL, content: Array[Byte]) {
  /**
   * Parsed outlinks from the content converted to a UTF-8 encoded string.
   */
  val outlinks: Seq[URL] = {
    val contentAsString = new String(content, "UTF-8")
    val linkRegex = """href="([\w\d\Q-._~:/?#[]@!$&'()*+,;=\E]*+)"""".r
    linkRegex.findAllIn(contentAsString).matchData
      .map(link => link.group(1))
      .toList.distinct.flatMap(link => Try(new URL(url, link)).toOption)
  }
}