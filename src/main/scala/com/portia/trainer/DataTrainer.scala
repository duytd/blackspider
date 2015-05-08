package com.portia.trainer

import com.mongodb.casbah.commons.MongoDBObject
import com.portia.algorithms.NaiveBayesClassifier
import com.portia.models._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import scala.util.Random

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
      val cSlug = category(2).trim
      if (!Category.existedCategory(cAlias)) {
        saveCategoryToDB(cAlias, cName, cSlug)
      }
    }
  }

  def run(): Unit = {
    val nbc:NaiveBayesClassifier = new NaiveBayesClassifier

    //Build training data set
    if (TrainingUrlDAO.find(MongoDBObject.empty).isEmpty)
      buildTrainingData()

    //Build testing data set
    if (TestingUrlDAO.find(MongoDBObject.empty).isEmpty)
      buildTestingData()

    //Build vocabulary tokens
    if (TokenDAO.find(MongoDBObject.empty).isEmpty) {
      val tokenizer = new Tokenizer()
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

    // Learn
    if (TokenScoreDAO.find(MongoDBObject.empty).isEmpty)
      nbc.learnNaiveBayesText()
  }

  def buildTestingData():Unit = {
    println("Building testing dataset...")

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

    println("Finish building testing dataset...")
  }

  def findTestingUrls(category: Category):ArrayBuffer[Url] = {
    val allUrls = UrlDAO.find(MongoDBObject.empty)
    var maxSize = 20
    var arrayBuffer = new ArrayBuffer[Url]()
    allUrls.foreach(url => {
      // If number of urls exceeds 20 then return
      if (maxSize == 0) {
        return arrayBuffer
      }

      // If url does not belong to training set and
      // belongs to current category then add it to array buffer
      if (TrainingUrlDAO.find(MongoDBObject("urlId"->url._id)).isEmpty
        && (determineCategoryByURL(url.absPath) == category.alias) && validTrainingUrl(url)) {
        maxSize = maxSize - 1
        arrayBuffer += url
      }
    })
    arrayBuffer
  }

  def buildTrainingData(): Unit = {
    val allUrls = UrlDAO.find(MongoDBObject.empty)
    val allCategories = CategoryDAO.find(MongoDBObject.empty)
    val docLimitSizes = new ArrayBuffer[(Category,Int)]()

    //build limit sizes array
    allCategories.foreach(cat => {
      if (cat.alias == "education") {
        docLimitSizes += ((cat, 300))
      }
      else {
        docLimitSizes += ((cat, getRandom()))
      }
    })

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
          if (!TrainingUrl.existedUrl(url._id) && validTrainingUrl(url))  {
            TrainingUrl.save(trainingUrl)
            println("Saved " +url.absPath+" - Category: "+category.name+" to training set")
            // update limit size
            docLimitSizes(index) = ((category, limit-1))
          }
        }
      }
    })
  }

  def validTrainingUrl(url: Url):Boolean = {
    !url.absPath.contains("video") && !url.absPath.contains("hot.html") && !url.absPath.contains("latest.html") && !url.absPath.contains("photo")
  }

  def determineCategoryByURL(absPath:String): String = {
    val urlPath = absPath
    val categories = CategoryDAO.find(MongoDBObject.empty)
    categories.foreach(category=>{
      val slugs = category.slug.split(',')
      if (containsSlug(urlPath, slugs)) {
        return category.alias
      }
    })
    null
  }

  def containsSlug(url:String, slugs:Array[String]):Boolean = {
    slugs.foreach(slug => {
      if (url.contains(slug.trim()))
        return true
    })
    false
  }

  def saveCategoryToDB(alias:String, name:String, slug:String): Unit = {
    val category =  new Category(alias = alias, name = name, slug = slug)
    CategoryDAO.insert(category)
    println("Created category: "+category.name)
  }

  def getRandom():Int = {
    val r:Random = new Random()
    val start = 275
    val end = 285
    r.nextInt(end-start) + start
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
}
