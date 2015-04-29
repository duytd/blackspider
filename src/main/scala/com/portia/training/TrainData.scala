package com.portia.training

import com.mongodb.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import com.portia.algorithms.NaiveBayesClassifier
import com.portia.models.{CategoryDAO, Category}
import models.DocumentDAO
import models.Document
import scala.io.Source

/**
 * @author qmha
 */
class TrainData {
  val categoryFilePath = "/docs/categories.txt"
  buildCategory()

  def buildCategory(): Unit = {
    for (line <- Source.fromURL(getClass.getResource(this.categoryFilePath), "UTF-8").getLines()) {
      val category = line.split('|')
      //category.foreach(println(_))
      val cAlias = category(0).trim
      val cName = category(1).trim
      val cSlug = if (category(2).trim!=null) category(2).trim else null
      if (!Category.existedCategory(cAlias)) {
        saveCategoryToDB(cAlias, cName)
      }
    }
  }

  def assignCategoryToDoc(doc:Document): Unit = {
    val urlPath = doc.url.absPath
    for (line <- Source.fromURL(getClass.getResource(this.categoryFilePath), "UTF-8").getLines()) {
      val category = line.split("|")

      if (category(2).trim != null) {
        val cSlug = category(2).trim
        if (urlPath.contains(cSlug)) {
          val cAlias = category(1).trim
          val categoryObj = Category.findByAlias(cAlias).get
          val updateDoc = doc.copy(categoryId = categoryObj._id)
          DocumentDAO.update(MongoDBObject("_id"->doc._id), updateDoc, upsert = false, multi = false, new WriteConcern)
        }
      }
    }
  }

  def train(): Unit = {
    var nbc:NaiveBayesClassifier = new NaiveBayesClassifier
    nbc.learnNaiveBayesText
  }

  def saveCategoryToDB(alias:String, name:String): Unit = {
    val category =  new Category(alias = alias, name = name)
    CategoryDAO.insert(category)
    println("Created category: "+category.name)
  }
}
