package com.portia.models
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatMongoCursor, SalatDAO}
import com.novus.salat.global._

/**
 * Tokens which are extracted from web document
 * @author duytd
 */

case class Token(_id: ObjectId = new ObjectId, name: String)

object TokenDAO extends SalatDAO[Token, ObjectId](
  collection = DB.mongoDB("tokens"))

object Token {
  def findAll(): SalatMongoCursor[Token] = TokenDAO.find(MongoDBObject.empty)

  def existedToken(tokenName:String):Boolean = {
    TokenDAO.findOne(MongoDBObject("name"->tokenName)).nonEmpty
  }
}