import config.Constants
import models.Crawler
import scala.concurrent.duration._
import org.jsoup.nodes.Element

/**
 * Created by duytd on 07/04/2015.
 */
object NewsMonitorMain {
  def main(args: Array[String]): Unit = {
    val system = akka.actor.ActorSystem("system")
    val targetUrls = Constants.TARGET_URLS
    targetUrls.foreach(url => {
      val newsMonitor = new NewsMonitor(url._2, url._1)
      //system.scheduler.schedule(0 milliseconds, 15 minutes)(newsMonitor.monitor())
      newsMonitor.monitor()
    })
  }
}
