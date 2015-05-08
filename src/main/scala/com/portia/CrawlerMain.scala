package com.portia

import com.portia.models.Crawler
import com.portia.models.{DBQueueDAO}
import com.portia.config.Constants
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Crawler main program
 * @author duytd
 */
object CrawlerMain {
  def main(args: Array[String]): Unit = {
    val targetUrls = Constants.TARGET_URLS

    // Extract target urls array and start crawling
    targetUrls.foreach(url=> {
      val queueSize = DBQueueDAO.count(MongoDBObject("rootUrl"->url._1))
      var isResumeMode = false
      if (queueSize > 0) {
        isResumeMode = true
      }

      val crawler = new Crawler(isResumeMode = isResumeMode, rootUrl = url._1)
      crawler.crawl()
    })
  }
}