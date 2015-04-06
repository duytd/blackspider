/**
 * Created by duytd on 04/03/2015.
 */

import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.ObjectId
import scala.collection.mutable.Queue

class Crawler(val rootUrl:String, isResumeMode:Boolean = false) {
  // Initialize the urls queue
  val urlQueue = new Queue[(ObjectId, String)]

  // If the site is crawled from the beginning, insert root url to the queue
  // Otherwise, load pending urls queue from the database to process
  if (isResumeMode == false) {
    val initialQueueItem = (new ObjectId, "http://"+rootUrl)
    urlQueue += initialQueueItem
    insertQueueItem(initialQueueItem)
    println("Start crawling "+rootUrl)
  }
  else {
    this.getQueueFromDB().foreach(item => {
      urlQueue += ((item.uid, item.url))
    })
    println("Resume crawling "+rootUrl)
  }

  def crawl():Unit = {
    // Stop crawling if the queue is empty
    try {
      if (urlQueue.isEmpty) {
        println("Finish crawling "+rootUrl)
        return;
      }

      // Dequeue to process
      val url = urlQueue.dequeue()
      this.removeQueueItem(url._1)

      // Save the processing url to the database
      saveUrlToDB(url._1, url._2)

      // Get all children of processing url
      val children = this.findChildrenUrls(url._2)

      for (i <- 0 until children.size) {
        val anchor = children(i)
        val newChild = anchor.asInstanceOf[Element].attr("href")
        val normalizedNewChild = Url.normalizeUrl(newChild,rootUrl)

        if (Url.isValid(newChild, rootUrl) && !existedQueueItem(normalizedNewChild) && !Url.existedUrl(normalizedNewChild)) {
          // Enqueue the child url
          val newQueueItem:(ObjectId, String) = (new ObjectId, normalizedNewChild)
          urlQueue += newQueueItem
          this.insertQueueItem(newQueueItem)

          // With each child, build the edge with its parent and save to database
          Edge.buildEdge(newQueueItem._1, url._1)
        }
      }
    }
    catch {
      case e: Exception => println("Exception caught: " + e.getMessage)
    }

    crawl()
  }

  def findChildrenUrls(url:String):Array[AnyRef] = {
    println("Parsing "+url+" to get child links...")
    val doc = Jsoup.connect(url).get()
    doc.select("a[href]").toArray
  }

  def insertQueueItem(item:(ObjectId, String)) = {
    val dbQueue = new DBQueue(uid = item._1, url = item._2, rootUrl = rootUrl)
    DBQueueDAO.insert(dbQueue)
    println("Insert "+dbQueue.url+ " to queue database")
  }

  def existedQueueItem(url:String): Boolean = {
    urlQueue.find(_._2 == url).nonEmpty
  }

  def removeQueueItem(uid:ObjectId) = {
    val dbQueue = DBQueueDAO.findOne(MongoDBObject("uid"->uid))
    DBQueueDAO.remove(dbQueue.get)
    println("Removed "+dbQueue.get.url+ " from queue database")
  }

  def getQueueFromDB():Array[DBQueue] = {
    DBQueueDAO.find(MongoDBObject.empty).toArray
  }

  def saveUrlToDB(uid:ObjectId, url:String): Unit = {
    val normalizedUrl = Url.normalizeUrl(url, rootUrl)
    val normalizedUrlObj = new Url(_id = uid, absPath = normalizedUrl, rootUrl = rootUrl)
    UrlDAO.insert(normalizedUrlObj)
    println("Crawled "+normalizedUrl)
  }
}
