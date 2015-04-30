package com.portia.models

import com.mongodb.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatMongoCursor
import org.jsoup.Jsoup
import com.github.nscala_time.time.Imports._

/** A dummy downloader which has a simple job of downloading content of crawled web nodes
  *
  * @author duytd
  */
class Downloader {
  def run(): Unit = {
    while(true){
      val freshUrls = UrlDAO.find(MongoDBObject("downloaded"->false))
      this.download(freshUrls)
    }
  }

  private def download(freshUrls:SalatMongoCursor[Url]): Unit = {
    for (url<-freshUrls) {
      Downloader.download(url)
    }
  }
}

/** Factory for [[com.portia.models.Downloader]] instances. */
object Downloader {
  def download(url:Url): Unit = {
    try {
      // Get the HTML body content
      val doc = Jsoup.connect(url.absPath).get.body()

      println("Downloading " + url.absPath + "...")

      // Mark as downloaded
      val updateUrl = url.copy(downloaded = true, parseTime = DateTime.now.toString)
      UrlDAO.update(MongoDBObject("_id"->url._id), updateUrl, upsert = false, multi = false, new WriteConcern)

      // Save content to the database
      val documentObj = new Document(urlId = url._id, content = doc.toString)
      DocumentDAO.insert(documentObj)

      // Tokenize document
      val lang = Url.getLang(url.rootUrl)
      val tokenizer = new Tokenizer(lang = lang)
      tokenizer.tokenizeDoc(documentObj)

      println("Finish downloading " + updateUrl.toString)
    }

    catch {
      case e: Exception => println("Failed to download "+url.absPath+". Reason:" + e.getMessage)
        val updateUrl = url.copy(downloaded = false)
        UrlDAO.update(MongoDBObject("_id"->url._id), updateUrl, upsert = false, multi = false, new WriteConcern)
    }
  }
}