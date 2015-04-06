package models

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.global._

/**
 * Created by duytd on 17/03/2015.
 */
case class Url (_id: ObjectId = new ObjectId, absPath:String = "", rootUrl:String = "", downloaded:Boolean = false, parseTime:String = null)

object UrlDAO extends SalatDAO[Url, ObjectId](
  collection = DB.mongoDB("urls"))

object Url {
  def isValid(url:String, rootUrl:String, force:Boolean = false): Boolean = {
    !isSpecialUrl(url, rootUrl) && !isRootUrl(url, rootUrl) && (belongsToRootUrl(url, rootUrl) || !isAbsoluteUrl(url))
  }

  /* Check whether an url belongs to root */
  def belongsToRootUrl(url:String, rootUrl:String): Boolean = {
    val urlRegex = "^([\\w+]+:\\/\\/)?(\\w+)(\\.\\w+)*".r
    val domain = urlRegex.findFirstIn(url).toString
    domain.contains(rootUrl)
  }

  def isAbsoluteUrl(url:String): Boolean = {
    val absoluteUrlRegex = "^(http(s)?:\\/\\/).*".r
    absoluteUrlRegex.pattern.matcher(url).matches()
  }

  def isRootUrl(url:String, rootUrl:String):Boolean = {
    val rootRegex = ("^(http(s)?\\:\\/\\/)?(\\w+\\.)?"+rootUrl).r
    rootRegex.pattern.matcher(url).matches()
  }

  /* check special url: rss url, hash, e-mail, telephone url and javascript */
  def isSpecialUrl(url:String, rootUrl:String):Boolean = {
    url.startsWith("#") || url.contains("rss") || url.contains("mailto")  ||
      url.contains("tel:") || url.contains("javascript")
  }

  def removePrefixesFromUrl(url:String): String = {
    val prefixesRegex = "(http(s)?:\\/\\/)(www\\.)?".r
    prefixesRegex.replaceAllIn(url,"")
  }

  //check whether and url existed in the database
  def existedUrl(url:String):Boolean = {
    UrlDAO.findOne(MongoDBObject("absPath"->url)).nonEmpty
  }

  //normalize url: relative link-> absolute link
  def normalizeUrl(url:String, rootUrl:String): String = {
    var initialUrl = Url.removePrefixesFromUrl(url)

    //remove trailing hashtag and slash
    if (initialUrl.last == '/' || initialUrl.last == '#') {
      initialUrl = initialUrl.dropRight(1)
    }

    if (Url.isAbsoluteUrl(url)) {
      "http://"+initialUrl
    }
    else {
      "http://"+rootUrl+initialUrl
    }
  }

}