package com.portia.trainer

import com.mongodb.casbah.commons.MongoDBObject
import com.portia.algorithms.NaiveBayesClassifier
import com.portia.models._
import com.portia.tokenizer.Tokenizer
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import com.portia.lib.Utils

/**
 * @author qmha, duytd
 */
class DataTrainer(lang:String = "en") {
  val categoryFilePath = if (lang == "en") "/docs/categories.txt" else "/docs/vi_categories.txt"

  def run(): Unit = {
    val nbc:NaiveBayesClassifier = new NaiveBayesClassifier

    //Build categories
    if (CategoryDAO.find(MongoDBObject.empty).isEmpty)
      buildCategory()

    //Build training data set
    if (TrainingUrlDAO.find(MongoDBObject.empty).isEmpty)
      buildTrainingData()

    //Build testing data set
    if (TestingUrlDAO.find(MongoDBObject.empty).isEmpty)
      buildTestingData()

    //Build vocabulary tokens
    if (TokenDAO.find(MongoDBObject.empty).isEmpty) {
      val tokenizer = new Tokenizer(lang = this.lang)
      val trainingDataSet = TrainingUrlDAO.find(MongoDBObject.empty).toArray
      val tds_size = trainingDataSet.length
      var t_count = 0
      trainingDataSet.foreach(data => {
        val doc = data.document
        print("Tokenizing: "+t_count+"/"+tds_size+"\r")
        tokenizer.tokenizeDoc(doc)
        t_count = t_count + 1
      })
    }

    // Train Portia classifier
    if (TokenScoreDAO.find(MongoDBObject.empty).isEmpty)
      nbc.learnNaiveBayesText()
  }

  /* Get category defined in /resource/docs/categories.txt and save to DB */
  def buildCategory(): Unit = {
    for (line <- Source.fromURL(getClass.getResource(this.categoryFilePath), "UTF-8").getLines()) {
      val category = line.split('|')
      val cAlias = category(0).trim
      val cName = category(1).trim
      val cSlug = category(2).trim
      if (!Category.existedCategory(cAlias)) {
        saveCategoryToDB(cAlias, cName, cSlug)
      }
    }
  }

  /* Build pre-categorized data to form a knowledge base */
  def buildTrainingData(): Unit = {
    val allUrls = UrlDAO.find(MongoDBObject.empty)
    val allCategories = CategoryDAO.find(MongoDBObject.empty)
    val docLimitSizes = new ArrayBuffer[(Category,Int)]()

    // Build limit sizes array
    // Each category will have 275 - 285 documents
    if (lang == "en") {
      allCategories.foreach(cat => {
        if (cat.alias == "education") {
          docLimitSizes += ((cat, 300))
        }
        else {
          docLimitSizes += ((cat, Utils.getRandom(275, 285)))
        }
      })
    }
    else {
      allCategories.foreach(cat => {
        docLimitSizes += ((cat, Utils.getRandom(85, 115)))
      })
    }

    allUrls.foreach(url => {
      if (docLimitSizes.find(_._2 > 0).isEmpty)
        return

      val categoryAlias = determineCategoryByURL(url.absPath)
      if (categoryAlias != null) {
        val dls = docLimitSizes.find(_._1.alias == categoryAlias).get
        val limit = dls._2
        val index = docLimitSizes.indexOf(dls)

        if (limit > 0) {
          val category = Category.findByAlias(categoryAlias).get
          val document = Document.getDocumentByUrl(url._id).get
          val trainingUrl = new TrainingUrl(urlId = url._id, categoryId = category._id, docId = document._id)

          // save to training set
          if (!TrainingUrl.existedUrl(url._id) && isValidTrainingUrl(url))  {
            TrainingUrl.save(trainingUrl)
            println("Saved " +url.absPath+" - Category: "+category.name+" to training set")
            // update limit size
            docLimitSizes(index) = ((category, limit-1))
          }
        }
      }
    })
  }

  def buildTestingData():Unit = {
    println("Building testing data set...")

    val allCategories = CategoryDAO.find(MongoDBObject.empty)
    allCategories.foreach(category => {
      val urls = findTestingUrls(category)
      urls.par.foreach(u => {
        if (!TestingUrl.existedUrl(u._id)) {
          val testingUrl = new TestingUrl(urlId = u._id, expectedResult = category._id)
          TestingUrl.save(testingUrl)
        }
      })
    })

    println("Finish building testing data set...")
  }

  def findTestingUrls(category: Category):ArrayBuffer[Url] = {
    val allUrls = UrlDAO.find(MongoDBObject.empty)
    var maxSize = if (this.lang == "en") 20 else 10
    var arrayBuffer = new ArrayBuffer[Url]()
    allUrls.foreach(url => {
      // If number of urls exceeds 20 then return
      if (maxSize == 0) {
        return arrayBuffer
      }

      // If url does not belong to training set and
      // belongs to current category then add it to array buffer
      if (TrainingUrlDAO.find(MongoDBObject("urlId"->url._id)).isEmpty
        && (determineCategoryByURL(url.absPath) == category.alias) && isValidTrainingUrl(url)) {
        maxSize = maxSize - 1
        arrayBuffer += url
      }
    })
    arrayBuffer
  }

  /* Training data must not belong to photo or video or common pages */
  def isValidTrainingUrl(url: Url):Boolean = {
    !url.absPath.contains("video") && !url.absPath.contains("hot.html") &&
      !url.absPath.contains("latest.html") && !url.absPath.contains("photo")
  }

  /* Pre-categorized url basing on their absolute path
  * Return category alias
  */

  def determineCategoryByURL(absPath:String): String = {
    val urlPath = absPath
    val categories = CategoryDAO.find(MongoDBObject.empty)
    categories.foreach(category=>{
      val slugs = category.slug.split(',')
      if (Utils.hasArrayElementInString(urlPath, slugs)) {
        return category.alias
      }
    })
    null
  }

  def saveCategoryToDB(alias:String, name:String, slug:String): Unit = {
    val category =  new Category(alias = alias, name = name, slug = slug)
    CategoryDAO.insert(category)
    println("Created category: "+category.name)
  }

}
