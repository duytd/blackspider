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

    //Tokenize all categorized docs
    val tokenizer = new Tokenizer()
    tokenizer.tokenizeMultiDocs(Document.getCategorizedDocs())

    // Normalize Token
    this.normalizeToken()

    // Assign category
    assignCategoryToExamples()

    // Learn
    nbc.learnNaiveBayesText()
  }

  def normalizeToken(): Unit = {
    println("Start normalizing token...")
    val tokens = Token.findAll().toArray
    //remove token if it is not a word or it has single character
    tokens.foreach(token => {
      if (!isWord(token.name) || token.name.length == 1) {
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
    val examples = DocumentDAO.find(MongoDBObject.empty)
    var d_count = 0
    val e_size = examples.length
    examples.foreach(doc => {
      val category = getCategoryByURL(doc.url.absPath)
      if (category != null) {
        val newDoc = doc.copy(categoryId = category._id)
        DocumentDAO.update(MongoDBObject("_id"->doc._id), newDoc, upsert = false, multi = false, new WriteConcern)
        d_count = d_count + 1
        print("Processing "+d_count+"/"+e_size+"\r")
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
