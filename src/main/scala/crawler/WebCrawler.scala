package crawler

import dispatch._
import Defaults._
//import scala.async.Async.{async, await}

/**
 * @author andrei
 */
object WebCrawler {
  def main(args: Array[String]): Unit = {
    println("Started")
    val query = url("http://api.hostip.info/country.php")
    val response = Http(query OK as.String)
    println("Continued")
    for (r <- response) println(r)
    println("Ended")
  }
}
