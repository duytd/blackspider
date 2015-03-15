package models

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._

/**
 * Created by duytd on 15/03/2015.
 */
case class Url (_id: ObjectId = new ObjectId, absPath:String, parseTime:Long)

object UrlDAO extends SalatDAO[Url, ObjectId](
  collection = MongoConnection()("blackspider")("urls"))
