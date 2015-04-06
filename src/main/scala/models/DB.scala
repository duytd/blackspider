package models

import com.mongodb.casbah.{MongoClient, MongoClientURI}

/**
 * Created by duytd on 02/04/2015.
 */
object DB {
  private val uri = MongoClientURI("mongodb://localhost")
  private val mongoClient =  MongoClient(uri)
  val mongoDB = mongoClient.getDB("blackspider")
}
