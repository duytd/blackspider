package com.portia.models

import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatMongoCursor, SalatDAO}
import com.novus.salat.global._

/**
 * A link between two web nodes
 * @author duytd
 */
case class Edge (_id: ObjectId = new ObjectId, source:ObjectId, target:ObjectId)

object Edge {
  //check whether the new edge is existed or not
  def existedEdge(child:ObjectId, parent:ObjectId): Boolean = {
    EdgeDAO.find(MongoDBObject("$and" -> (MongoDBObject("source"->parent),
      MongoDBObject("target"->child)))).nonEmpty
  }

  def findAll(): SalatMongoCursor[Edge] = EdgeDAO.find(MongoDBObject.empty)

  def buildEdge(child:ObjectId, parent:ObjectId): Unit = {
    if (!Edge.existedEdge(child = child, parent = parent) && child != parent) {
        val edge = new Edge(source = parent, target = child)
        EdgeDAO.insert(edge)
        println("Built edge between "+child+" and "+parent)
      }
  }
}

object EdgeDAO extends SalatDAO[Edge, ObjectId](
  collection = DB.mongoDB("edges"))
