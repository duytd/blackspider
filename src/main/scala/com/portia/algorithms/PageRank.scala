package com.portia.algorithms

import com.mongodb.casbah.commons.MongoDBObject
import com.portia.models.{EdgeDAO, UrlDAO, Url}
import scala.collection.mutable.ArrayBuffer
import com.mongodb.casbah.Imports._

/**
 * @author qmha, duytd
 */
class PageRank {
  val c:Double = 0.85
  var iterations:Int = 1000
  var urls: Array[Url] = UrlDAO.find(MongoDBObject.empty).toArray

  def run(): Unit = {
    for (i <- 0 until iterations) {
      println("Iteration: " + i)

      // Loop through all url in the DB
      (0 until urls.length).par.foreach(i => {
        var pr = 0.15
        // Loop through all pages that link to this one
        val linkers = getEdgesByUrl(urls(i)._id)
        linkers.foreach(linker => {
          // Get page rank of the linker
          val linkerPageRank = linker.pageRank
          // Get the total number of links (incoming links and
          // outgoing links from the linker)
          val linkerCount = getEdgesByUrl(linker._id).size
          // Page rank
          pr += c * (linkerPageRank / linkerCount)
        })

        // Update page rank in the database
        updatePageRank(urls(i)._id, pr)
      })
    }
  }

  /* Update page rank of each url in DB */
  def updatePageRank(url_id: ObjectId, pageRank:Double) = {
    val url = Url.findById(url_id).get
    val updatedUrl = url.copy(pageRank = pageRank)
    UrlDAO.update(MongoDBObject("_id"->url_id), updatedUrl, upsert = false, multi = false, new WriteConcern)
  }

  /* Get all node which are linked to an specific url */
  def getEdgesByUrl(url_id: ObjectId):ArrayBuffer[Url] = {
    var results:ArrayBuffer[Url] = new ArrayBuffer[Url]()
    val edges = EdgeDAO.find(MongoDBObject("$or"->(MongoDBObject("source"->url_id),
      MongoDBObject("target"->url_id)))).toList

    edges.foreach(edge => {
      if (edge.source == url_id && edge.target != url_id) {
        results += Url.findById(edge.target).get
      }

      if (edge.source != url_id && edge.target == url_id) {
        results += Url.findById(edge.source).get
      }
    })
    results
  }
}
