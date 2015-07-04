package crawler

import dispatch._, Defaults._
//import scala.async.Async.{async, await}

/**
 * @author andrei
 */
object WebCrawler {
  private val frontier = URLFrontier("")
  def main(args: Array[String]): Unit = {
    println("Started")
    val query = url("http://api.hostip.info/country.php")
    val response = Http(query OK as.String)
    println("Continued")
    for (r <- response) println(r)
    println("Ended")
  }
}
