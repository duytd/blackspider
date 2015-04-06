import com.mongodb.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import models._
import org.jsoup.Jsoup
import com.github.nscala_time.time.Imports._

/**
 * Created by duytd on 17/03/2015.
 */
class Downloader {
  def start: Unit = {
    while(true){
      val freshUrls = UrlDAO.find(MongoDBObject("downloaded"->false))
      for (url<-freshUrls) {
        downloadData(url)
      }
    }
  }

  def downloadData(url:Url): Unit = {
    try {
      //get the HTML content
      val doc = Jsoup.connect(url.absPath).get()

      println("Downloading " + url.absPath + "...")

      //mark as downloaded
      val updateUrl = url.copy(downloaded = true, parseTime = DateTime.now.toString)
      UrlDAO.update(MongoDBObject("_id"->url._id), updateUrl, upsert = false, multi = false, new WriteConcern)

      //save content to the database
      val documentObj = new Document(urlId = url._id, content = doc.toString)
      DocumentDAO.insert(documentObj)

      println("Finish downloading " + updateUrl.toString)
    }

    catch {
      case e: Exception => println("Failed to download "+url.absPath+". Reason:" + e.getMessage)
        val updateUrl = url.copy(downloaded = false)
        UrlDAO.update(MongoDBObject("_id"->url._id), updateUrl, upsert = false, multi = false, new WriteConcern)
    }
  }
}
