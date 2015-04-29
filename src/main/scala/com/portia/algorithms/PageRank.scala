package com.portia.algorithms

import models.Url

import scala.collection.mutable.ArrayBuffer
import com.mongodb.casbah.Imports._

/**
 * @author qmha
 */
class PageRank {
  var iterations:Int = 10000
  var urls: ArrayBuffer[Url] = getUrls

  def run: Unit = {
    for (i <- 0 until iterations) {
      println("Iteration " + i)

      // For all URL
      urls.foreach(url => {
        val pr = 0.15
        // Loop through all pages that link to this one
        val linkers = getEdgesByUrl(url._id)
        linkers.foreach(linker => {
          // Get the pagerank of the linker
          
        })
      })
    }
  }

  def getUrls:ArrayBuffer[Url] = {
    new ArrayBuffer[Url]()
  }

  def getEdgesByUrl(url_id: ObjectId):ArrayBuffer[Url] = {
    0
  }
}
