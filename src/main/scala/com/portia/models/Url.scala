package com.portia.models

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.global._
import com.portia.config.Constants

/**
 * Single web node
 * @author duytd
 */
case class Url (_id: ObjectId = new ObjectId, absPath:String = "", rootUrl:String = "", downloaded:Boolean = false, parseTime:String = null, pageRank:Double = 1.0)

object UrlDAO extends SalatDAO[Url, ObjectId](
  collection = DB.mongoDB("urls"))

object Url {
  def isValid(url:String, rootUrl:String, force:Boolean = false): Boolean = {
    !isSpecialUrl(url) && !isRootUrl(url, rootUrl) && (belongsToRootUrl(url, rootUrl) || !isAbsoluteUrl(url))
  }

  def isAbsoluteUrl(url:String): Boolean = {
    val absoluteUrlRegex = "^(http(s)?:\\/\\/).*".r
    absoluteUrlRegex.pattern.matcher(url).matches()
  }

  def isRootUrl(url:String, rootUrl:String):Boolean = {
    val rootRegex = ("^(http(s)?\\:\\/\\/)?(\\w+\\.)?"+rootUrl).r
    rootRegex.pattern.matcher(url).matches()
  }

  /* Check whether an url belongs to root */
  def belongsToRootUrl(url:String, rootUrl:String): Boolean = {
    val urlRegex = "^([\\w+]+:\\/\\/)?(\\w+)(\\.\\w+)*".r
    val domain = urlRegex.findFirstIn(url).toString
    domain.contains(rootUrl)
  }

  /* A function to check special url: rss url, hash, e-mail, telephone url and javascript */
  def isSpecialUrl(url:String):Boolean = {
    url.startsWith("#") || url.contains("rss") || url.contains("mailto")  ||
      url.contains("tel:") || url.contains("javascript")
  }

  /* A function to remove http(s) protocol and www hostname */
  def removePrefixesFromUrl(url:String): String = {
    val prefixesRegex = "(http(s)?:\\/\\/)(www\\.)?".r
    prefixesRegex.replaceAllIn(url,"")
  }

  def normalizeUrl(url:String, rootUrl:String): String = {
    var initialUrl = Url.removePrefixesFromUrl(url)

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

  /* A function to check whether an url existed in the database */
  def existedUrl(url:String):Boolean = {
    UrlDAO.findOne(MongoDBObject("absPath"->url)).nonEmpty
  }

  def find(url:String):Option[Url] = {
    UrlDAO.findOne(MongoDBObject("absPath"->url))
  }

  def findById(id:ObjectId):Option[Url] = {
    UrlDAO.findOne(MongoDBObject("_id"->id))
  }

  def saveUrlToDB(uid:ObjectId, url:String, rootUrl:String): Url = {
    val normalizedUrl = Url.normalizeUrl(url, rootUrl)
    val normalizedUrlObj = new Url(_id = uid, absPath = normalizedUrl, rootUrl = rootUrl)
    UrlDAO.insert(normalizedUrlObj)
    println("Crawled "+normalizedUrl)
    normalizedUrlObj
  }

  def getLang(rootUrl:String):String = {
    Constants.TARGET_URLS.foreach(item => {
      if (item._1 == rootUrl) {
        return item._3
      }
    })
    "en"
  }
}