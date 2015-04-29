package com.portia.algorithms

import models.{UrlDAO, Edge, Url}

import scala.collection.mutable.ArrayBuffer
import com.mongodb.casbah.Imports._

/**
 * @author qmha
 */
class PageRank {
  val c:Double = 0.85
  var iterations:Int = 10000
  var urls: Array[Url] = getUrls

  def run: Unit = {
    for (i <- 0 until iterations) {
      println("Iteration " + i)

      // For all URL
      urls.foreach(url => {
        var pr = 0.15
        // Loop through all pages that link to this one
        val linkers = getEdgesByUrl(url._id)
        linkers.foreach(linker => {
          // Get the pagerank of the linker
          val linkingpr = linker.pageRank
          // Get the total number of links from the linker
          val linkingcount = getEdgesByUrl(linker._id).size
          // Pagerank
          pr += c * (linkingpr / linkingcount)
        })

        // Update pagerank to database
        updatePageRank(url._id, pr)
      })
    }
  }

  def updatePageRank(url_id: ObjectId, pageRank:Double) = {

  }

  def getUrls:Array[Url] = {
    UrlDAO.find(MongoDBObject.empty).toArray
  }

  def getEdgesByUrl(url_id: ObjectId):ArrayBuffer[Url] = {
    var results:ArrayBuffer[Url] = new ArrayBuffer[Url]()
    val edges = Edge.findAll().toArray
    edges.foreach(edge => {
      if (edge.vertexes(0) == url_id && edge.vertexes(1) != url_id) {
        results += Url.findById(edge.vertexes(1)).get
      }

      if (edge.vertexes(0) != url_id && edge.vertexes(1) == url_id) {
        results += Url.findById(edge.vertexes(0)).get
      }
    })

    results
  }
}
