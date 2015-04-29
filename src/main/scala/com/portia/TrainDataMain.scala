package com.portia

import _root_.models.DocumentDAO
import com.mongodb.casbah.commons.MongoDBObject
import com.portia.algorithms.{NaiveBayesClassifier, PageRank}
import com.portia.training.TrainData
import org.jsoup.Jsoup

/**
 * Created by duytd on 29/04/2015.
 */
object TrainDataMain {
  def main(args: Array[String]): Unit = {
    val trainData = new TrainData
    val ranking = new PageRank
    val nbc = new NaiveBayesClassifier

    // train token score
    trainData.train()

    //testClassifier()
  }

  def testClassifier() = {
    val nbc = new NaiveBayesClassifier
    // get latest document
    //val document = DocumentDAO.findOne(MongoDBObject.empty).get
    val document = Jsoup.connect("http://dantri.com.vn/su-kien/hlv-miura-cong-bo-18-cau-thu-tap-trung-doi-tuyen-quoc-gia-1065884.htm").get().body().text()
        // try to classify
    val result = nbc.classifyNBC(document)

    println("Result: " + result.name)
  }
}
