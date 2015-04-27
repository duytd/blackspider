package models

import java.util

import com.mongodb.casbah.Imports._
import com.novus.salat.dao.{SalatDAO}
import com.novus.salat.global._

/**
 * Tokens which are extracted from web document
 * @author duytd
 */

case class DocTokens(_id: ObjectId = new ObjectId, documentId: ObjectId, tokens:util.ArrayList[String])

object DocTokensDAO extends SalatDAO[DocTokens, ObjectId](
  collection = DB.mongoDB("doc_tokens_collection"))