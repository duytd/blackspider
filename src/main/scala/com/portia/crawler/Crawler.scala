package com.portia.models

import com.novus.salat.dao.SalatMongoCursor

import scala.collection.mutable.Queue
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.ObjectId

/** A powerful crawler which can crawl a specific website  on the Internet
  *
  * @constructor create a crawler with a rootUrl and resumeMode option
  * @param rootUrl root web node (E.g wikipedia.com,cnn.com,...)
  * @param isResumeMode decide whether the crawler will crawl from the beginning or not
  * @author minhhq, duytd
  */
class Crawler(isResumeMode:Boolean = false, rootUrl:String) {
  // Initialize an empty urls queue
  val urlQueue = new Queue[(ObjectId, String, ObjectId)] // childId, child url, parentId
  var count = 0
  val currentTime = System.currentTimeMillis()

  // If the site is crawled from the beginning, insert root url to the queue
  // Otherwise, load pending urls queue from the database to process
  if (!this.isResumeMode) {
    val initialQueueItem = (new ObjectId, "http://"+rootUrl, null)
    urlQueue += initialQueueItem
    Crawler.insertQueueItem(initialQueueItem, rootUrl)
    println("Start crawling "+rootUrl)
  }
  else {
    this.getQueueFromDB.foreach(item => {
      urlQueue += ((item.uid, item.url, item.parentId))
    })
    println("Resume crawling "+rootUrl)
  }

  def crawl():Unit = {
    // Stop crawling if the queue is empty
    try {
      if (urlQueue.isEmpty) {
        println("Finish crawling "+rootUrl)
        return
      }

      // Dequeue a node from queue to crawl
      val url = urlQueue.dequeue()
      this.removeQueueItem(url._1)

      // Save this to DB
      Url.saveUrlToDB(url._1, url._2, rootUrl)
      count = count + 1

      // Build edges
      Edge.buildEdge(url._1, url._3)

      if ((System.currentTimeMillis() - currentTime) == 1000) {
        println(count)
        return
      }

      // Get all children of the node
      val children = Crawler.fetchChildrenUrls(url._2)

      for (i <- 0 until children.length) {
        val anchor = children(i)
        val newChild = anchor.asInstanceOf[Element].attr("href")
        val normalizedNewChild = Url.normalizeUrl(newChild,rootUrl)

        //if the child not existed in queue and db, insert it the queue
        if (Url.isValid(newChild, rootUrl) && !existedQueueItem(normalizedNewChild)
          && !Url.existedUrl(normalizedNewChild)) {
          // Enqueue the child node
          val newQueueItem:(ObjectId, String, ObjectId) = (new ObjectId, normalizedNewChild, url._1)
          urlQueue += newQueueItem
          Crawler.insertQueueItem(newQueueItem, rootUrl)
        }
      }
    }
    catch {
      case e: Exception => println("Exception caught: " + e.getMessage)
    }

    crawl()
  }

  private def existedQueueItem(url:String): Boolean = {
    urlQueue.find(_._2 == url).nonEmpty && DBQueue.existedDBQueue(url)
  }

  private def removeQueueItem(uid:ObjectId) = {
    val dbQueue = DBQueueDAO.findOne(MongoDBObject("uid"->uid)).get
    DBQueueDAO.remove(dbQueue)
    println("Removed "+dbQueue.url+ " from queue database")
  }

  private def getQueueFromDB:SalatMongoCursor[DBQueue] = {
    DBQueueDAO.find(MongoDBObject.empty)
  }
}

/** Factory for [[com.portia.models.Crawler]] instances. */
object Crawler {
  /** Put pending nodes to the database so that they can be accessed later
    */
  def insertQueueItem(item:(ObjectId, String, ObjectId), rootUrl:String) = {
    val dbQueue = new DBQueue(uid = item._1, parentId = item._3, url = item._2, rootUrl = rootUrl)
    DBQueueDAO.insert(dbQueue)
    println("Insert "+dbQueue.url+ " to queue database")
  }

  def fetchChildrenUrls(url:String):Array[AnyRef] = {
    println("Parsing "+url+" to get child links...")
    val doc = Jsoup.connect(url).get()
    doc.select("a[href]").toArray
  }

  def fetchRssLinkItems(url:String):Array[AnyRef] = {
    println("Parsing "+url+" to get child links...")
    val doc = Jsoup.connect(url).ignoreContentType(true).get()
    doc.select("link").toArray
  }
}
