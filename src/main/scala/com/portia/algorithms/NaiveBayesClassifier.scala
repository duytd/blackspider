package com.portia.algorithms

import java.util

import com.mongodb.casbah.commons.MongoDBObject
import models._
import org.jsoup.Jsoup

import scala.collection.mutable.ArrayBuffer

import com.portia.models.{CategoryDAO, TokenScoreDAO, TokenScore, Category}
import com.mongodb.casbah.Imports.ObjectId

/**
 * @author qmha
 */
class NaiveBayesClassifier {
  var examples:Array[Document] = getExamples()
  var vocabulary:Array[Token] = getVocabulary
  var categories:Array[Category] = getCategories()

  def classifyNBC(document: String): Category = {
    var VNB:ArrayBuffer[(Category, Double)]= new ArrayBuffer[(Category, Double)]()
    val tokenizer = new Tokenizer
    val tokens:util.ArrayList[String] = tokenizer.tokenize(Jsoup.parse(document).text())

    categories.foreach(category => {
      var v_j:Double = 1
      val doc_j = Document.getDocumentsByCategory(category._id)
      val P_vj:Double = doc_j.size.toDouble / examples.size.toDouble

      for (i <- 0 until tokens.size()) {
        // find this token
        val token = TokenDAO.findOne(MongoDBObject("name"->tokens.get(i)))
        if (token != None) {
          // get this score
          val score = TokenScoreDAO.findOne(
            MongoDBObject("$and"->(MongoDBObject("tokenId"->token.get._id), MongoDBObject("categoryId"->category._id)))).get

          v_j = v_j.toDouble * score.score.toDouble

          println("Classifying token:" + token.get.name + ", with score:" + score.score)
        }
      }

      v_j = v_j * P_vj

      println("Score of category " + category.name + ": " + v_j)

      // Save
      VNB += ((category, v_j))
    })

    // Return max
    VNB.sortWith(_._2 >= _._2)(0)._1
  }

  def learnNaiveBayesText:Unit = {
    // For each category
    categories.foreach(category => {
      var doc_j = Document.getDocumentsByCategory(category._id)
      var P_vj:Double = doc_j.size.toDouble / examples.size.toDouble
      var Text_j = concatenateDocumentByCategory(doc_j)
      var n = Text_j.distinct.size

      //println("Category :" + category.name + " has " + doc_j.size + " documents")
      //println("Examples = " + examples.size + ". Vocab = " + vocabulary.size + ". Total Tokens:" + Text_j.size + ". Distinct: " + n + ". P_vj = " + P_vj)

      vocabulary.foreach(wk => {
        val n_k = Text_j.count(_ == wk)
        //Text_j.foreach(item => {print(item + " ")})
        var ct:Int = 0
        val P:Double = (n_k + 1).toDouble / (n + vocabulary.size).toDouble

        //println("V = " + vocabulary.size + ". NK = " + n_k)

        println("Training token: " + wk.name + " with score: " + P)
        // Insert into database
        insertIntoDatabase(wk._id, category._id, P)
      })
    })
  }

  def insertIntoDatabase(tokenId: ObjectId, categoryId: ObjectId, score: Double) = {
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
    DocumentDAO.find(MongoDBObject.empty).toArray
  }

  def getCategories():Array[Category] = {
    CategoryDAO.find(MongoDBObject.empty).toArray
  }
}
