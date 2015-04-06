package models

import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._

/**
 * Created by duytd on 15/03/2015.
 */
case class DBQueue(_id: ObjectId = new ObjectId, uid:ObjectId, url: String, rootUrl:String)

object DBQueueDAO extends SalatDAO[DBQueue, ObjectId](
  collection = DB.mongoDB("queues"))
