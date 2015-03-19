package models

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._

/**
 * Created by duytd on 15/03/2015.
 */
case class CrawledUrl (_id: ObjectId = new ObjectId, absPath:String, rootUrl:String, parseTime:Long) extends Url

object CrawledUrlDAO extends SalatDAO[CrawledUrl, ObjectId](
  collection = MongoConnection()("blackspider")("crawled_urls"))
