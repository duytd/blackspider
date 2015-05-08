package com.portia

import com.mongodb.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import com.portia.algorithms.NaiveBayesClassifier
import com.portia.models._

/**
 * Classifier main program
 * @author duytd
 */
object ClassifierMain {
  def main(args:Array[String]): Unit ={
    val nbc = new NaiveBayesClassifier()
    val testingUrls = TestingUrlDAO.find(MongoDBObject.empty).toArray
    val testSize = testingUrls.length
    var correctResult = 0

    testingUrls.foreach(u => {

      if (u.actualResult == null) {
        val url = Url.findById(u.urlId).get
        println("Testing " +url.absPath)
        val possibleCategory:Category = nbc.classifyDoc(Document.getDocumentByUrl(url._id).get)

        if (u.expectedResult == possibleCategory._id) {
          println("Correct !")
        }
        else {
          println("Failed !")
        }

        val updatedTestingUrl = u.copy(actualResult = possibleCategory._id)
        TestingUrlDAO.update(MongoDBObject("_id"->u._id), updatedTestingUrl, upsert = false, multi = false, new WriteConcern)
      }

    })


    TestingUrlDAO.find(MongoDBObject.empty).foreach(u => {
      if (u.actualResult == u.expectedResult) {
        correctResult = correctResult + 1
      }
    })

    println("Correct: "+correctResult+". Failed: "+testSize - failedResult+".Accuracy: "+correctResult.toDouble/testSize.toDouble*100+"%")
  }
}
