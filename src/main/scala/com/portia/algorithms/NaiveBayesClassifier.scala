package com.portia.algorithms

import com.portia.models._
import com.portia.tokenizer.Tokenizer
import com.portia.models.{CategoryDAO, TokenScoreDAO, TokenScore, Category}
import scala.collection.mutable.ArrayBuffer
import com.portia.lib.Utils
import org.jsoup.Jsoup
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.ObjectId

/**
 * Implement Naive Bayes Algorithm to classify web documents
 * @author qmha, duytd
 */
class NaiveBayesClassifier {
  var examples:Array[TrainingUrl] = this.getExamples()
  var vocabulary:Array[Token] = this.getVocabulary
  var categories:Array[Category] = this.getCategories()

  /* Train the Portia Classifier
* Pseudo code of the algorithm:
*
* For each category (which Portia need to predict)
*   doc_j is all documents belongs to current category in this iteration
*   P_vj is the ratio between doc_j size and the examples(all pre-categorized data) size
*   Text_j is the array of token which is made my concatenate all documents in doc_j
*   n is number of elements in Text_j
*   For each token in vocabulary
*     n_k is the frequency of current token (in this iteration) in Text_j
*     Then: P (score of token) will be calculate by the formula:
*       (n_k + 1)/(n + |vocabulary|)
*/

  def learnNaiveBayesText() = {
    // For each category
    categories.par.foreach(category => {
      val doc_j:Array[TrainingUrl] = TrainingUrl.getTrainingDataByCategory(category._id)
      val P_vj: Double = doc_j.length.toDouble / examples.length.toDouble

      // Update the P_vj number in all categories
      val updatedCategory = category.copy(Pvj = P_vj)
      Category.update(category._id, updatedCategory)

      val Text_j: ArrayBuffer[String] = concatenateTrainingDataByCategory(doc_j)
      val n = Text_j.size
      var t_count = 0
      val v_size = vocabulary.length

      println("Training category :" + category.name + " which has " + doc_j.length + " documents")
      vocabulary.par.foreach(wk => {
        // Count the frequency of wk in Text_j
        val n_k = Text_j.count(_ == wk.name)

        // We have the token score
        val P: Double = (n_k + 1).toDouble / (n + vocabulary.length).toDouble
        t_count = t_count + 1
        print("Processing token: "+t_count+"/"+v_size+"\r")

        // Insert into database
        if (!TokenScore.existedTokenScore(wk._id, category._id)) {
          saveTokenScoreToDB(wk._id, category._id, P)
        }
      })
    })
  }

  /* Classify document
  * Pseudo code of the algorithm:
  *
  * First we tokenize the document to make an array of tokens called T
  * For each category (which Portia need to predict)
  *   P_vj is the number which is calculated in NaiveBayesText method
  *   (If P_vj is not available, we stop the method)
  *   We get the score of each token in T, then multiply all of them
  *   together to get a number called v_j. However, because the token score
  *   is very small which can lead to underflow error (-Infinity), we use the logarithm
  *   of token score by base 10 to calculate v_j
  *   log10(a*b) = log10(a)+log10(b)
  * The predicted category is the category which has max value among v_j results
  */
  def classifyNBC(htmlDoc: String): Category = {
    var VNB:ArrayBuffer[(Category, Double)]= new ArrayBuffer[(Category, Double)]()
    val tokenizer = new Tokenizer
    val tokens:ArrayBuffer[String] = tokenizer.tokenize(Utils.html2text(htmlDoc))

    categories.par.foreach(category => {
      var v_j:Double = 0
      val P_vj:Double = category.Pvj

      if (P_vj == -1.0) {
        println("Please train the example data set first before classifying !")
        return null
      }
      else {
        //println("Examining category:" + category.name)

        tokens.par.foreach(tokenName => {
          // Find this token
          val token = TokenDAO.findOne(MongoDBObject("name"->tokenName))
          if (token != None) {
            // Get the token score
            val tokenScore = TokenScoreDAO.findOne(
              MongoDBObject("$and"->(MongoDBObject("tokenId"->token.get._id),
                MongoDBObject("categoryId"->category._id)))).get
            v_j = v_j + Math.log10(tokenScore.score)
          }
        })

        v_j = v_j + Math.log10(P_vj)

        //println("Score of category " + category.name + ": " + v_j)

        VNB += ((category, v_j))
      }

    })

    // Return max
    VNB.sortWith(_._2 >= _._2).head._1
  }

  /* Classify an input url string and return possible result */
  def classifyPageByUrl(url:String): Category = {
    val document = Jsoup.connect(url).get().body().text()
    val result = this.classifyNBC(document)
    result
  }

  /* Classify a Document object and return possible result */
  def classifyDoc(doc: Document): Category = {
    this.classifyNBC(doc.content)
  }

  /* Concatenate all documents which belong to a category and make an array of tokens */
  def concatenateTrainingDataByCategory(trainingData: Array[TrainingUrl]):ArrayBuffer[String] = {
    var result:ArrayBuffer[String] = new ArrayBuffer[String]()
    val tokenizer = new Tokenizer()

    // Get document of each url, tokenize them and group all to a big array
    trainingData.par.foreach(data =>{
      tokenizer.tokenize(Utils.html2text(data.document.content)).foreach(item => {
        result += item
      })
    })
    result
  }

  /* Save token score to DB so that it can be used to classify document */
  def saveTokenScoreToDB(tokenId: ObjectId, categoryId: ObjectId, score: Double) = {
    val tokenScore = new TokenScore(tokenId = tokenId, categoryId = categoryId, score = score)
    TokenScoreDAO.insert(tokenScore)
  }

  /* Get all token from db to make a vocabulary of distinct words */
  def getVocabulary:Array[Token] = {
    TokenDAO.find(MongoDBObject.empty).toArray
  }

  /* Get all urls which will be used to train Portia Classifier */
  def getExamples():Array[TrainingUrl] = {
    TrainingUrlDAO.find(MongoDBObject.empty).toArray
  }

  /* Get all categories from DB */
  def getCategories():Array[Category] = {
    CategoryDAO.find(MongoDBObject.empty).toArray
  }
}
