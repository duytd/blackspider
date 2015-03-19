import com.mongodb.casbah.commons.MongoDBObject
import models.{EdgeUtils, Edge, EdgeDAO}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by duytd on 15/03/2015.
 */
object BlackSpiderApp {

  def main(args: Array[String]): Unit = {
    //list of initial urls
    val rootUrls = Array("vnexpress.net", "dantri.com.vn")
    var crawler = new Crawler;
    crawler.start(rootUrls)

    var downloader = new Downloader
    downloader.start()

  }
}