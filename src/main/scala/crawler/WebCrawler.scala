package crawler

import scala.async.Async.{async, await}
import dispatch._
import Defaults._

/**
 * @author andrei
 */
object WebCrawler {
  def read(url:String):String = io.Source.fromURL(url).getLines.mkString

  def main(args: Array[String]): Unit = {
    val rules = robotstxt.RuleSet(Seq("/abc", "/"), Nil)
    println("Started")
    println(read("https://github.com"))
    val query = url("http://api.hostip.info/country.php")
    val response = Http(query OK as.String)
    println("Continued")
    for (r <- response) println(r)
    println("Ended")
  }
}
