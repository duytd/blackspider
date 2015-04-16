package models

import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._

/**
 * Pending web nodes which will be dequeue later to crawl
 * @author duytd
 */
case class DBQueue(_id: ObjectId = new ObjectId, uid:ObjectId, url: String, rootUrl:String)

object DBQueueDAO extends SalatDAO[DBQueue, ObjectId](
  collection = DB.mongoDB("queues"))
