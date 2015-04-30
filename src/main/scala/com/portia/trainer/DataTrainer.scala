package com.portia.trainer

import com.mongodb.casbah.commons.MongoDBObject
import com.portia.algorithms.NaiveBayesClassifier
import com.portia.models.{CategoryDAO, Category}
import com.portia.models.DocumentDAO
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
      val cSlug = if (category(2).trim!=null) category(2).trim else null
      if (!Category.existedCategory(cAlias)) {
        saveCategoryToDB(cAlias, cName)
      }
    }
  }

  def assignCategoryToExamples(): Unit = {
    val examples = DocumentDAO.find(MongoDBObject.empty).limit(EXAMPLES_LIMIT_SIZE).toArray
    examples.foreach(doc => {
      assignCategoryByURL(doc.url.absPath)
    })
  }

  def assignCategoryByURL(absPath:String): ObjectId = {
    val urlPath = absPath
    for (line <- Source.fromURL(getClass.getResource(this.categoryFilePath), "UTF-8").getLines()) {
      val category = line.split('|')

      if (category(2).trim != null) {
        val cSlug = category(2).trim
        if (urlPath.contains(cSlug)) {
          val cAlias = category(0).trim
          val categoryObj = Category.findByAlias(cAlias).get
          return categoryObj._id
        }
      }
    }
    null
  }

  def run(): Unit = {
    val nbc:NaiveBayesClassifier = new NaiveBayesClassifier
    nbc.learnNaiveBayesText()
  }

  def saveCategoryToDB(alias:String, name:String): Unit = {
    val category =  new Category(alias = alias, name = name)
    CategoryDAO.insert(category)
    println("Created category: "+category.name)
  }
}
