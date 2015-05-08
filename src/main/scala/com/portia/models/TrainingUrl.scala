package com.portia.models

import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatDAO}
import com.novus.salat.global._

/**
 * Created by duytd on 07/05/2015.
 */

case class TrainingUrl (_id: ObjectId = new ObjectId, urlId: ObjectId, categoryId: ObjectId, docId: ObjectId) {
  def document: Document = {
    Document.findById(this.docId).get
  }
}

object TrainingUrlDAO extends SalatDAO[TrainingUrl, ObjectId](
  collection = DB.mongoDB("training_urls"))

object TrainingUrl {
  def getTrainingDataByCategory(id: ObjectId):Array[TrainingUrl] = {
    TrainingUrlDAO.find(MongoDBObject("categoryId" -> id)).toArray
  }

  def save(trainingUrl: TrainingUrl): Unit = {
    TrainingUrlDAO.insert(trainingUrl)
  }

  /* A function to check whether an url existed in the database */
  def existedUrl(uid:ObjectId):Boolean = {
    TrainingUrlDAO.findOne(MongoDBObject("urlId"->uid)).nonEmpty
  }
}

