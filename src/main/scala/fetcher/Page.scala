package fetcher

/**
 * @author andrei
 */
final case class Page(url: String, content: Array[Byte]) {
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