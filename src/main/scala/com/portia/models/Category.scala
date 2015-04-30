package com.portia.models

import com.mongodb.WriteConcern
import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.global._

/**
 * @author qmha
 */
case class Category(_id: ObjectId = new ObjectId, alias:String, name:String, parentId:ObjectId = new ObjectId, Pvj:Double = -1.0)

object CategoryDAO extends SalatDAO[Category, ObjectId](
  collection = DB.mongoDB("categories"))

object Category {
  // check whether the category existed or not
  def existedCategory(alias:String):Boolean = {
    CategoryDAO.findOne(MongoDBObject("alias"->alias)).nonEmpty
  }

  // find category by alias
  def findByAlias(alias:String):Option[Category] = {
    CategoryDAO.findOne(MongoDBObject("alias"->alias))
  }

  def update(id:ObjectId, updatedCategory:Category): Unit = {
    CategoryDAO.update(MongoDBObject("_id"->id), updatedCategory, upsert = false, multi = false, new WriteConcern)
  }
}

