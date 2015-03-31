/**
 * Created by duytd on 15/03/2015.
 */
object CrawlerMain {

  def main(args: Array[String]): Unit = {
    //list of initial urls
    val rootUrls = Array("vnexpress.net", "dantri.com.vn")
    val crawler = new Crawler
    crawler.start(rootUrls)
  }
}