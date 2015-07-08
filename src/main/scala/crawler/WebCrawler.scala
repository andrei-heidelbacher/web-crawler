package crawler

import fetcher._
import java.io.{File, PrintWriter}
import rx.lang.scala.{Subscription, Observable}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success}

/**
 * @author andrei
 */
object WebCrawler {
  val userAgent = {
    val agentName: String = "HHbot"
    val userAgentString: String = agentName +
      " https://github.com/andrei-heidelbacher/web-crawler"
    UserAgent(agentName, userAgentString)
  }

  val fetcher = PageFetcher(userAgent.userAgentString)

  def crawl(initial: Traversable[String]): Observable[Page] = {
    Observable.create[Page]({ logger =>
      val frontier = URLFrontier(userAgent, initial)
      var count = 0
      while (count < 1000) {
        if (!frontier.isEmpty) {
          val url = frontier.pop()
          val page = fetcher.fetch(url)
          count += 1
          page.onComplete {
            case Success(p) =>
              logger.onNext(p)
              p.outlinks.foreach(frontier.tryPush)
            case _ => ()
          }
        }
      }
      logger.onCompleted()
      Subscription {}
    })
  }

  def main(args: Array[String]): Unit = {
    val args = Array[String]("history.txt", "http://www.reddit.com/")
    for {
      fileName <- Try(args(0))
      initial <- Try(args.tail)
    } {
      val writer = new PrintWriter(new File(fileName))
      val pageStream = crawl(initial)
      val logger = pageStream.subscribe { page =>
        println(page.url)
        writer.println(page.url)
        writer.flush()
      }
      logger.unsubscribe()
      writer.close()
      println("Finished!")
    }
  }
}
