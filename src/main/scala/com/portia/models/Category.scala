package com.portia.models

import models.{DB, Url}

/**
 * @author qmha
 */
case class Category(_id: ObjectId = new ObjectId, name:String, parentId:ObjectId = new ObjectId) {
}
object CategoryDAO extends SalatDAO[Document, ObjectId](
  collection = DB.mongoDB("categories"))
