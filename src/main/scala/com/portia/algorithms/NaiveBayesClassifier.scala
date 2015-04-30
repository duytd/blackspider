package com.portia.algorithms

import com.portia.models._
import com.portia.models.{CategoryDAO, TokenScoreDAO, TokenScore, Category}
import java.util.ArrayList
import scala.collection.mutable.ArrayBuffer
import org.jsoup.Jsoup
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.ObjectId

/**
 * @author qmha
 */
class NaiveBayesClassifier {
  val EXAMPLES_LIMIT_SIZE = 10000
  var examples:Array[Document] = getExamples()
  var vocabulary:Array[Token] = getVocabulary
  var categories:Array[Category] = getCategories()
  val PvjFilePath = "/docs/Pvj.txt"

  def classifyNBC(text: String): Category = {
    var VNB:ArrayBuffer[(Category, Double)]= new ArrayBuffer[(Category, Double)]()
    val tokenizer = new Tokenizer
    val tokens:ArrayList[String] = tokenizer.tokenize(Jsoup.parse(text).text())

    categories.foreach(category => {
      var v_j:Double = 1
      val P_vj = category.Pvj

      if (P_vj == -1.0) {
        println("Please train the example data set first before classifying !")
        return null
      }
      else {
        println("Examining category:" + category.name)

        for (i <- 0 until tokens.size()) {
          // find this token
          val token = TokenDAO.findOne(MongoDBObject("name"->tokens.get(i)))
          if (token != None) {
            // get this score
            val score = TokenScoreDAO.findOne(
              MongoDBObject("$and"->(MongoDBObject("tokenId"->token.get._id), MongoDBObject("categoryId"->category._id)))).get
            v_j = v_j * score.score
          }
        }

        v_j = v_j * P_vj

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
    categories.foreach(category => {
      var doc_j = Document.getDocumentsByCategory(category._id)
      var P_vj: Double = doc_j.length.toDouble / examples.length.toDouble

      val updatedCategory = category.copy(Pvj = P_vj)
      Category.update(category._id, updatedCategory)

      var Text_j = concatenateDocumentByCategory(doc_j)
      var n = Text_j.distinct.size

      println("Training category :" + category.name + " has " + doc_j.length + " documents")

      vocabulary.foreach(wk => {
        val n_k = Text_j.count(_ == wk)
        var ct: Int = 0
        val P: Double = (n_k + 1).toDouble / (n + vocabulary.length).toDouble
        // Insert into database
        if (!TokenScore.existedTokenScore(wk._id, category._id)) {
          saveTokenScoreToDB(wk._id, category._id, P)
        }
      })
    })
  }

  def classifyPageByUrl(url:String): Category = {
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

  def concatenateDocumentByCategory(documents: Array[Document]):ArrayBuffer[String] = {
    var result:ArrayBuffer[String] = new ArrayBuffer[String]()
    val tokenizer = new Tokenizer()
    documents.foreach(doc=>{
      tokenizer.tokenize(Jsoup.parse(doc.content).text()).toArray.foreach(item => {
        result += item.toString
      })
    })
    result
  }

  def getVocabulary:Array[Token] = {
    TokenDAO.find(MongoDBObject.empty).toArray
  }

  def getExamples():Array[Document] = {
    DocumentDAO.find(MongoDBObject.empty).limit(EXAMPLES_LIMIT_SIZE).toArray
  }

  def getCategories():Array[Category] = {
    CategoryDAO.find(MongoDBObject.empty).toArray
  }
}
