package com.portia.models

import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatDAO}
import com.novus.salat.global._

/**
 * Web node html content
 * @author duytd
 */
case class Document(_id: ObjectId = new ObjectId, urlId: ObjectId, content:String, tokenized:Boolean = false, categoryId:ObjectId = null) {
  def url: Url = {
    Url.findById(this.urlId).get
  }
}
object DocumentDAO extends SalatDAO[Document, ObjectId](
  collection = DB.mongoDB("documents"))

object Document {
  def getDocumentByUrl(uid: ObjectId):Option[Document] = {
    DocumentDAO.findOne(MongoDBObject("urlId" -> uid))
  }

  def getCategorizedDocs(): Array[Document] ={
    DocumentDAO.find(MongoDBObject("categoryId"->MongoDBObject("$ne"->None))).toArray
  }

  def findById(id:ObjectId):Option[Document] = {
    DocumentDAO.findOne(MongoDBObject("_id"->id))
  }
}
