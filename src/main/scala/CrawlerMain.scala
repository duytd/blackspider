/**
 * Created by duytd on 15/03/2015.
 */

import com.mongodb.casbah.commons.MongoDBObject
import config.Constants
import models.{DBQueueDAO, Url}

object CrawlerMain {

  def main(args: Array[String]): Unit = {
    val rootUrls = Constants.ROOT_URLS
    rootUrls.foreach(url=> {
      val queueSize = DBQueueDAO.count(MongoDBObject("rootUrl"->url))
      var isResumeMode = false
      if (queueSize > 0) {
        isResumeMode = true
      }
      val crawler = new Crawler(url, isResumeMode = isResumeMode)
      crawler.crawl()
    })
  }
}