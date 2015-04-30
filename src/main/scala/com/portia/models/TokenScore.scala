package com.portia.models
import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._

/**
 * @author duytd
 */
case class TokenScore(_id: ObjectId = new ObjectId, tokenId: ObjectId, categoryId: ObjectId, score: Double)

object TokenScoreDAO extends SalatDAO[TokenScore, ObjectId](
  collection = DB.mongoDB("token_scores"))

object TokenScore {
  /* A function to check whether a token score existed in the database */
  def existedTokenScore(tokenId:ObjectId, categoryId: ObjectId): Boolean = {
    TokenScoreDAO.findOne(MongoDBObject("$and"->(MongoDBObject("tokenId"->tokenId),MongoDBObject("categoryId"->categoryId)))).nonEmpty
  }
}
