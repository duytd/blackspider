package com.portia.models
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.mongodb.casbah.Imports.ObjectId
/** A rss monitor which has duty to monitor and updated latest news of a web root(rootUrl)
  *
  * @constructor create a monitor with a rssSource, rootUrl and running crawler
  * @param rootUrl root web node (E.g wikipedia.com,cnn.com,...)
  * @param rssSource Rss source link of a website (E.g http://edition.cnn.com/services/rss/,...)
  * @author duytd
  */

class NewsMonitor(val rssSource:String, val rootUrl:String) {
  var rssSet:Set[String] = Set()

  def run(): Unit = {
    try {
      val rssCategories = this.getRssCategories()
      rssCategories.foreach(category=> {
        fetchNews(category)
      })
      println("Finish updating...")
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

      // Save un-existed news to the queues database to crawl later
      if(!Url.existedUrl(url) && !DBQueue.existedDBQueue(url)) {
        val newQueueItem:(ObjectId, String) = (new ObjectId, url)
        Crawler.insertQueueItem(newQueueItem, rootUrl)
      }
    })
  }
}
