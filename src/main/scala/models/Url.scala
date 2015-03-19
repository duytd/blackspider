package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.{ValidBSONType, MongoDBObject}
import com.novus.salat.dao.SalatDAO
import models.Url
import org.bson.types.ObjectId

/**
 * Created by duytd on 17/03/2015.
 */
case class Url (_id: ObjectId = new ObjectId, absPath:String, rootUrl:String, downloaded:Boolean = false, parseTime:Long = 0)

object UrlDAO extends SalatDAO[Url, ObjectId](
  collection = MongoConnection()("blackspider")("urls"))

object Url {
  def isValidLink(url:String, rootUrl:String): Boolean = {
    //rss link
    if (url.startsWith("/rss") || url.startsWith("#") || url.contains("mailto") || url.contains("tel:") || url=="http://"+rootUrl || url=="https://"+rootUrl ||
      url=="http://www."+rootUrl || url=="https://www."+rootUrl || (url.contains("http") && !url.contains(rootUrl)) || url.contains("javascript")){
      false
    }
    else {
      true
    }
  }

  def existedUrl(url:String):Any = {
    UrlDAO.findOne(MongoDBObject("absPath"->url))
  }

  def processUrl(url:String, rootUrl:String): String = {
    var initialUrl = url;
    val prefixes = Array("http://", "https://", "www.")
    var check = false

    //remove prefixes
    for (prefix<-prefixes) {
      if (url.contains(prefix)) {
        initialUrl = initialUrl.replaceAll(prefix, "")
        check = true
      }
    }

    //remove trailing hashtag and slash
    if (url.last == '/' || url.last == '#') {
      url.dropRight(1)
    }

    if (check==false) {
      "http://"+rootUrl+initialUrl
    }
    else {
      "http://"+initialUrl
    }
  }
}