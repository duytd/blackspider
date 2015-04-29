package com.portia.models

import models.DB
import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.global._


/**
 * @author qmha
 */
case class Category(_id: ObjectId = new ObjectId, alias:String, name:String, parentId:ObjectId = new ObjectId)

object CategoryDAO extends SalatDAO[Category, ObjectId](
  collection = DB.mongoDB("categories"))

object Category {
  def existedCategory(alias:String):Boolean = {
    CategoryDAO.findOne(MongoDBObject("alias"->alias)).nonEmpty
  }

  def findByAlias(alias:String):Option[Category] = {
    CategoryDAO.findOne(MongoDBObject("alias"->alias))
  }
}

