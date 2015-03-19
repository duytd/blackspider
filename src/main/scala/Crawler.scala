/**
 * Created by duytd on 04/03/2015.
 */

import com.mongodb.casbah.commons.MongoDBObject
import models._
import org.jsoup.Jsoup
class Crawler {

  def start(rootUrls:Array[String]): Unit = {
    for (rootUrl<-rootUrls) {
      buildUrlList("http://"+rootUrl,"",rootUrl)
    }
  }

  def buildUrlList(url:String, sourceUrl:String, rootUrl:String): Unit = {
    try {
      //get the HTML content
      var doc = Jsoup.connect(url).get()

      //get link
      var anchors = doc.select("a[href]").toArray

      if (Url.existedUrl(url) == null) {
        //save url to fresh list
        var urlObj = new Url(absPath = url, rootUrl = rootUrl)
        UrlDAO.insert(urlObj)
      }
      else {
        var urlObj = Url.existedUrl(url)
      }

      var sourceUrlObj = Url.existedUrl(sourceUrl)

      //check edge
      if (Edge.existedEdge(Array(urlObj._id, sourceUrlObj._id))) {
        var edge = new Edge(vertexes = Array(urlObj._id, sourceUrlObj._id))
        EdgeDAO.insert(edge)
      }

      //continue crawling other links in the HTML
      if (anchors.length > 0) {
        for (anchor <- anchors) {
          var url = anchor.asInstanceOf[org.jsoup.nodes.Element].attr("href")
          if (Url.isValidLink(url, rootUrl)) {
            var formalUrl = Url.processUrl(url.toString, rootUrl)
            //build list
            if (Url.existedUrl(formalUrl) == null) {
              buildUrlList(formalUrl,url, rootUrl)
            }
          }
        }
      }
      else {
        println("Finish crawling " + url);
      }
    }
    catch {
      case e: Exception => println("exception caught: " + e);
    }
  }

}
