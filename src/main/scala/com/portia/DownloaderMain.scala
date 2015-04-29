import models.{Document, Downloader}
import org.jsoup.Jsoup

/**
 * Downloader main program
 * @author duytd
 */
object DownloaderMain {
  def main(args: Array[String]): Unit = {
    var downloader = new Downloader
    //downloader.start
    val doc = Jsoup.connect("http://vnexpress.net/tin-tuc/oto-xe-may/mazdaspeed3-hatchback-the-thao-sap-ra-doi-3207352.html").get().text()
    println(Document.extractArticle(doc))
  }
}
