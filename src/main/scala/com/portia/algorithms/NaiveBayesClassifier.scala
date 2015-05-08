package com.portia.algorithms

import com.portia.models._
import com.portia.models.{CategoryDAO, TokenScoreDAO, TokenScore, Category}
import scala.collection.mutable.ArrayBuffer
import org.jsoup.Jsoup
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.ObjectId

/**
 * Implement Naive Bayes Algorithm to classify web documents
 * @author qmha
 */
class NaiveBayesClassifier {
  /* Examples is the set of first 10000 pages in the database*/
  var examples:Array[TrainingUrl] = getExamples()
  var vocabulary:Array[Token] = getVocabulary
  var categories:Array[Category] = getCategories()
  val PvjFilePath = "/docs/Pvj.txt"

  def classifyNBC(text: String): Category = {
    var VNB:ArrayBuffer[(Category, Double)]= new ArrayBuffer[(Category, Double)]()
    val tokenizer = new Tokenizer
    val tokens:ArrayBuffer[String] = tokenizer.tokenize(Jsoup.parse(text).text())

    categories.par.foreach(category => {
      var v_j:Double = 0
      val P_vj:Double = category.Pvj

      if (P_vj == -1.0) {
        println("Please train the example data set first before classifying !")
        return null
      }
      else {
        println("Examining category:" + category.name)

        tokens.par.foreach(tokenName => {
          // find this token
          val token = TokenDAO.findOne(MongoDBObject("name"->tokenName))
          if (token != None) {
            // get this score
            val tokenScore = TokenScoreDAO.findOne(
              MongoDBObject("$and"->(MongoDBObject("tokenId"->token.get._id),
                MongoDBObject("categoryId"->category._id)))).get
            v_j = v_j + Math.log10(tokenScore.score)
          }
        })

        v_j = v_j + Math.log10(P_vj)

        println("Score of category " + category.name + ": " + v_j)

        // Save
        VNB += ((category, v_j))
      }

    })

    // Return max
    VNB.sortWith(_._2 >= _._2).head._1
  }

  def learnNaiveBayesText() = {
    // For each category
    categories.par.foreach(category => {
      val doc_j:Array[TrainingUrl] = TrainingUrl.getTrainingDataByCategory(category._id)
      val P_vj: Double = doc_j.length.toDouble / examples.length.toDouble

      val updatedCategory = category.copy(Pvj = P_vj)
      Category.update(category._id, updatedCategory)

      val Text_j: ArrayBuffer[String] = concatenateTrainingDataByCategory(doc_j)
      val n = Text_j.size
      var t_count = 0
      val v_size = vocabulary.length

      println("Training category :" + category.name + " has " + doc_j.length + " documents")
      vocabulary.par.foreach(wk => {
        val n_k = Text_j.count(_ == wk.name)

        val P: Double = (n_k + 1).toDouble / (n + vocabulary.length).toDouble
        // Insert into database
        t_count = t_count + 1
        print("Token: "+t_count+"/"+v_size+"\r")

        if (!TokenScore.existedTokenScore(wk._id, category._id)) {
          saveTokenScoreToDB(wk._id, category._id, P)
        }
      })
    })
  }

  def classifyPageByUrl(url:String): Category = {
    println("Testing url "+url)
    val document = Jsoup.connect(url).get().body().text()
    val result = this.classifyNBC(document)
    result
  }

  def classifyDoc(doc: Document): Category = {
    this.classifyNBC(doc.content)
  }

  def saveTokenScoreToDB(tokenId: ObjectId, categoryId: ObjectId, score: Double) = {
    val tokenScore = new TokenScore(tokenId = tokenId, categoryId = categoryId, score = score)
    TokenScoreDAO.insert(tokenScore)
  }

  def concatenateTrainingDataByCategory(trainingData: Array[TrainingUrl]):ArrayBuffer[String] = {
    var result:ArrayBuffer[String] = new ArrayBuffer[String]()
    val tokenizer = new Tokenizer()
    trainingData.par.foreach(data =>{
      tokenizer.tokenize(Jsoup.parse(data.document.content).text()).toArray.foreach(item => {
        result += item
      })
    })
    result
  }

  def getVocabulary:Array[Token] = {
    TokenDAO.find(MongoDBObject.empty).toArray
  }

  def getExamples():Array[TrainingUrl] = {
    TrainingUrlDAO.find(MongoDBObject.empty).toArray
  }

  def getCategories():Array[Category] = {
    CategoryDAO.find(MongoDBObject.empty).toArray
  }
}
