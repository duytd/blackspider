package com.portia.models

import com.mongodb.casbah.{MongoClient, MongoClientURI}

/**
 * MongoDB connection configuration
 * @author duytd
 */
object DB {
  private val uri = MongoClientURI("mongodb://localhost")
  private val mongoClient =  MongoClient(uri)
  val mongoDB = mongoClient.getDB("blackspider_test")
}
