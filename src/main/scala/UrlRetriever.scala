  /**
   * Created by duytd on 04/03/2015.
   */
  import org.jsoup.Jsoup
  class UrlRetriever {

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

    def getUrlList(rootUrls:Array[String]): Unit = {
      for (rootUrl<-rootUrls) {
        var doc = Jsoup.connect("http://"+rootUrl).get()
        var anchors = doc.select("a[href]").toArray
        println("Parsing "+rootUrl+"...")
        for(anchor<-anchors) {
          var url = anchor.asInstanceOf[org.jsoup.nodes.Element].attr("href")

          if (isValidLink(url, rootUrl)) {
            var formalUrl = processUrl(url.toString, rootUrl)
            println(formalUrl)
          }
        }
      }
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
