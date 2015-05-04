package com.portia.trainer

import com.mongodb.casbah.commons.MongoDBObject
import com.portia.algorithms.NaiveBayesClassifier
import com.portia.models._
import scala.io.Source
import com.mongodb.casbah.Imports._

/**
 * @author qmha
 */
class DataTrainer {
  val EXAMPLES_LIMIT_SIZE = 10000
  val categoryFilePath = "/docs/categories.txt"
  buildCategory()

  def buildCategory(): Unit = {
    for (line <- Source.fromURL(getClass.getResource(this.categoryFilePath), "UTF-8").getLines()) {
      val category = line.split('|')
      val cAlias = category(0).trim
      val cName = category(1).trim
      if (!Category.existedCategory(cAlias)) {
        saveCategoryToDB(cAlias, cName)
      }
    }
  }

  def run(): Unit = {
    val nbc:NaiveBayesClassifier = new NaiveBayesClassifier
    //normalize Token
    this.normalizeToken()

    //assign category
    //assignCategoryToExamples()

    //learn
    nbc.learnNaiveBayesText()
  }

  def normalizeToken(): Unit = {
    println("Start normalizing token...")
    val tokens = Token.findAll().toArray
    //remove token if it is not a word
    tokens.foreach(token => {
      if (!isWord(token.name)) {
        TokenDAO.remove(token)
      }
    })
    println("Finish normalizing token !")
  }

  def isWord(str: String):Boolean = {
    str.foreach(c => {
      if (c.isDigit) {
        return false
      }
    })
    true
  }

  def assignCategoryToExamples(): Unit = {
    val examples = DocumentDAO.find(MongoDBObject.empty).limit(EXAMPLES_LIMIT_SIZE)
    examples.foreach(doc => {
      val category = getCategoryByURL(doc.url.absPath)
      if (category != null) {
        val newDoc = doc.copy(categoryId = category._id)
        DocumentDAO.update(MongoDBObject("_id"->doc._id), newDoc, upsert = false, multi = false, new WriteConcern)
        println("Assigned "+doc.url.absPath+" to "+category.name)
      }
    })
  }

  def getCategoryByURL(absPath:String): Category = {
    val urlPath = absPath
    for (line <- Source.fromURL(getClass.getResource(this.categoryFilePath), "UTF-8").getLines()) {
      val category = line.split('|')
      val slugs = category(2).split(',')
      if (containsSlug(urlPath, slugs)) {
        val cAlias = category(0).trim
        val categoryObj = Category.findByAlias(cAlias).get
        return categoryObj
      }
    }
    null
  }

  def containsSlug(url:String, slugs:Array[String]):Boolean = {
    slugs.foreach(slug => {
      if (url.contains(slug.trim()))
        return true
    })
    false
  }

  def saveCategoryToDB(alias:String, name:String): Unit = {
    val category =  new Category(alias = alias, name = name)
    CategoryDAO.insert(category)
    println("Created category: "+category.name)
  }
}
