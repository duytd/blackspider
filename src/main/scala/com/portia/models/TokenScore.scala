package com.portia.models
import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import models.DB
import com.novus.salat.global._
/**
 * Created by duytd on 29/04/2015.
 */
case class TokenScore(_id: ObjectId = new ObjectId, tokenId: ObjectId, categoryId: ObjectId, score:Double)

object TokenScoreDAO extends SalatDAO[TokenScore, ObjectId](
  collection = DB.mongoDB("token_scores"))
