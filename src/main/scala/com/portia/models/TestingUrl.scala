package com.portia.models

import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatDAO}
import com.novus.salat.global._

/**
 * Created by duytd on 07/05/2015.
 */

case class TestingUrl (_id: ObjectId = new ObjectId, urlId:ObjectId, expectedResult: ObjectId, actualResult: ObjectId = null)

object TestingUrlDAO extends SalatDAO[TestingUrl, ObjectId](
  collection = DB.mongoDB("testing_urls"))

object TestingUrl {
  def save(testingUrl: TestingUrl): Unit = {
    TestingUrlDAO.insert(testingUrl)
  }

  def existedUrl(uid:ObjectId):Boolean = {
    TestingUrlDAO.findOne(MongoDBObject("urlId"->uid)).nonEmpty
  }
}

