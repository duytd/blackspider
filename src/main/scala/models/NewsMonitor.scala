import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.mongodb.casbah.Imports.ObjectId
/** A rss monitor which has duty to monitor and updated latest news of a web root(rootUrl)
  *
  * @constructor create a monitor with a rssSource and rootUrl
  * @param rootUrl root web node (E.g wikipedia.com,cnn.com,...)
  * @param rssSource Rss source link of a website (E.g http://edition.cnn.com/services/rss/,...)
  * @author duytd
  */

class NewsMonitor(val rssSource:String, val rootUrl:String) {
  var rssSet:Set[String] = Set()

  def monitor(): Unit = {
    try {
      val rssCategories = this.getRssCategories()
      rssCategories.foreach(category=> {
        fetchNews(category)
      })
    }
    catch {
      case e: Exception => println("Exception caught: " + e.getMessage)
    }
  }

  def getRssCategories(): Set[String] = {
    val rssRegex = "^.*\\.rss$"
    val doc = Jsoup.connect(rssSource).get()
    doc.select("a[href~="+rssRegex+"]").toArray.foreach(category => {
      val href = Url.normalizeUrl(category.asInstanceOf[Element].attr("href"), rootUrl)
      rssSet += href
    })
    rssSet
  }

  def fetchNews(rssCategory:String): Unit = {
    val news = Crawler.fetchRssLinkItems(rssCategory)

    news.foreach(anchor => {
      val url = Url.normalizeUrl(anchor.asInstanceOf[Element].nextSibling().toString.trim(), rootUrl)

      if(!Url.existedUrl(url)) {
        // Save to database
        val newsObj = Url.saveUrlToDB(new ObjectId, url, rootUrl)

        // Download html content
        Downloader.download(newsObj)

        // Build edges
        val children = Crawler.fetchChildrenUrls(url)
        children.foreach(child => {
          val normalizedChild = Url.normalizeUrl(child.asInstanceOf[Element].attr("href"), rootUrl)
          val existedUrl = Url.find(normalizedChild)
          if (existedUrl.nonEmpty) {
            Edge.buildEdge(existedUrl.get._id, newsObj._id)
          }
          else {
            // Save un-existed children to the queues database to crawl later
            val newQueueItem:(ObjectId, String) = (new ObjectId, normalizedChild)
            Crawler.insertQueueItem(newQueueItem, rootUrl)
          }
        })
      }
    })
  }
}
