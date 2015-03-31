/**
 * Created by duytd on 04/03/2015.
 */

import models._
import org.jsoup.Jsoup
class Crawler {

  def start(rootUrls:Array[String]): Unit = {
    for (rootUrl<-rootUrls) {
      val rootUrlObj = new Url(absPath = "http://"+rootUrl, rootUrl = rootUrl)
      buildUrlList(rootUrlObj, rootUrl)
    }
  }

  def buildUrlList(targetUrl:Url, rootUrl:String, sourceUrl:Option[Url] = None): Unit = {
    try {
      val source = sourceUrl.orNull
      //get the HTML content

      //check edge
      if (source != null) {
        if (!Edge.existedEdge(Array(targetUrl._id, source._id))) {
          val edge = new Edge(vertexes = Array(targetUrl._id, source._id))
          EdgeDAO.insert(edge)
          println("Built edge between "+targetUrl.absPath+" and "+source.absPath)
        }
      }

      val doc = Jsoup.connect(targetUrl.absPath).get()

      //get link
      val anchors = doc.select("a[href]").toArray

      //continue crawling other links in the HTML
      if (anchors.length > 0) {
        for (anchor <- anchors) {
          val childUrl = anchor.asInstanceOf[org.jsoup.nodes.Element].attr("href")
          if (Url.isValid(childUrl, rootUrl)) {
            val normalizedUrl = Url.normalizeUrl(childUrl, rootUrl)

            if (!Url.existedUrl(normalizedUrl)) {
              val normalizedUrlObj = new Url(absPath = normalizedUrl, rootUrl = rootUrl)
              UrlDAO.insert(normalizedUrlObj)
              println("Crawled "+normalizedUrl)
              buildUrlList(normalizedUrlObj, rootUrl, Option(targetUrl))
            }
          }
        }
      }
    }
    catch {
      case e: Exception => println("Exception caught: " + e.getMessage)
    }
  }

}
