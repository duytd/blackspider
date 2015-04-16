package models

import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._

/**
 * Pending web nodes which will be dequeue later to crawl
 * @author duytd
 */
case class DBQueue(_id: ObjectId = new ObjectId, uid:ObjectId, url: String, rootUrl:String)

object DBQueueDAO extends SalatDAO[DBQueue, ObjectId](
  collection = DB.mongoDB("queues"))

object DBQueue {
  /* A function to check whether a db queue existed in the database */
  def existedDBQueue(url:String):Boolean = {
    DBQueueDAO.findOne(MongoDBObject("url"->url)).nonEmpty
  }
}