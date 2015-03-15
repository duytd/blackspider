  /**
   * Created by duytd on 04/03/2015.
   */

  import com.mongodb.casbah.commons.MongoDBObject
  import models.{DocumentDAO, Document, Url, UrlDAO}
  import org.jsoup.Jsoup
  class Crawler {

    def start(rootUrls:Array[String]): Unit = {
      for (rootUrl<-rootUrls) {
        crawlData("http://"+rootUrl, rootUrl)
      }
    }

    def crawlData(url:String, rootUrl:String): Unit = {
      println("Crawling "+url+"...")

      //get the HTML content
      var doc = Jsoup.connect(url).get()

      //get link
      var anchors = doc.select("a[href]").toArray

      //save url to the database
      var urlObj = new Url(absPath=url, parseTime = System.currentTimeMillis())
      UrlDAO.insert(urlObj)

      //save content to the database
      var documentObj = new Document(urlId=urlObj._id, content=doc.toString)
      DocumentDAO.insert(documentObj)

      //continue crawling other links in the HTML
      if (anchors.length > 0) {
        var valid = anchors.length
        for(anchor<-anchors) {
          var url = anchor.asInstanceOf[org.jsoup.nodes.Element].attr("href")
          if (isValidLink(url, rootUrl) && !isDuplicateLink(url)) {
            var formalUrl = processUrl(url.toString, rootUrl)
            //crawl data
            crawlData(formalUrl, rootUrl)
          }
        }
      }
      else {
        println("Finish crawling "+rootUrl);
      }
    }

    def isValidLink(url:String, rootUrl:String): Boolean = {
      //rss link
      if (url.startsWith("/rss") || url.startsWith("#") || url.contains("mailto") || url=="http://"+rootUrl || url=="https://"+rootUrl ||
        url=="http://www."+rootUrl || url=="https://www."+rootUrl || (url.contains("http") && !url.contains(rootUrl)) || url.contains("javascript:void(0)")){
        false
      }
      else {
        true
      }
    }

    def isDuplicateLink(url:String):Boolean = {
      !UrlDAO.find(MongoDBObject("absPath" ->url)).isEmpty
    }

    def processUrl(url:String, rootUrl:String): String = {
      var initialUrl = url;
      val prefixes = Array("http://", "https://", "www.")
      var check = false

      for (prefix<-prefixes) {
        if (url.contains(prefix)) {
          initialUrl = initialUrl.replaceAll(prefix, "")
          check = true
        }
      }

      if (check==false) {
        "http://"+rootUrl+initialUrl
      }
      else {
        "http://"+initialUrl
      }
    }
  }