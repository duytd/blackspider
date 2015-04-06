package models

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._

/**
 * Created by duytd on 15/03/2015.
 */
case class Document(_id: ObjectId = new ObjectId, urlId: ObjectId, content:String)

object DocumentDAO extends SalatDAO[Document, ObjectId](
  collection = DB.mongoDB("documents"))
