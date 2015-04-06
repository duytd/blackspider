package models

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatMongoCursor, SalatDAO}
import com.novus.salat.global._

import scala.io.Source

/**
 * Created by duytd on 15/03/2015.
 */
case class Edge (_id: ObjectId = new ObjectId, vertexes:Array[ObjectId])

object Edge {
  //check whether the new edge is existed or not
  def existedEdge(vt:Array[ObjectId]): Boolean = {
    EdgeDAO.find(ref = MongoDBObject("vertexes" -> MongoDBObject("$all" -> vt))).nonEmpty
  }

  def findAll(): SalatMongoCursor[Edge] = EdgeDAO.find(MongoDBObject.empty)

  def buildEdge(child:ObjectId, parent:ObjectId): Unit = {
    if (!Edge.existedEdge(Array(child, parent))) {
        val edge = new Edge(vertexes = Array(child, parent))
        EdgeDAO.insert(edge)
        println("Built edge between "+child+" and "+parent)
      }
  }
}

object EdgeDAO extends SalatDAO[Edge, ObjectId](
  collection = DB.mongoDB("edges"))
