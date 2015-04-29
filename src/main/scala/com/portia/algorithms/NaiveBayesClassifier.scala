package com.portia.algorithms

import java.util

import models.{Tokenizer, DocTokens, Document}

import scala.collection.mutable.ArrayBuffer

import com.portia.models.Category

/**
 * @author qmha
 */
class NaiveBayesClassifier {
  var examples:ArrayBuffer[Document] = new ArrayBuffer[Document]()
  var vocabulary:ArrayBuffer[DocTokens] = new ArrayBuffer[DocTokens]()
  var categories:ArrayBuffer[Category] = new ArrayBuffer[Category]()

  examples = getExamples
  vocabulary = getVocabulary
  categories = getCategories

  def classifyNBC(document: Document): Category = {
    var tokenizer = new Tokenizer
    var tokens:util.ArrayList[String] = tokenizer.tokenize(document.content)
    vocabulary.foreach(token => {

    })
  }

  def learnNaiveBayesText = {
    // For each category
    categories.foreach(category => {
      var doc_j = getDocumentByCategory(category._id)
      var P_vj = doc_j.size / examples.size
      var Text_j = concatenateDocumentByCategory(doc_j)
      var n = Text_j.distinct.size
      vocabulary.foreach(wk => {
        val n_k = Text_j.count(_ == wk)
        val P:Double = (n_k + 1) / (n + vocabulary.size)

        // Insert into database
        insertIntoDatabase(wk._id, category._id, P)
      })
    })
  }

  def insertIntoDatabase(tokenId: ObjectId, categoryId: ObjectId, score: Double) = {

  }

  def getDocumentByCategory(_id: ObjectId):ArrayBuffer[Document] = {
    new ArrayBuffer[Document]()
  }

  def concatenateDocumentByCategory(documents: ArrayBuffer[Document]):ArrayBuffer[DocTokens] = {
    new ArrayBuffer[DocTokens]()
  }

  def getVocabulary:ArrayBuffer[DocTokens] = {
    new ArrayBuffer[DocTokens]()
  }

  def getExamples():ArrayBuffer[Document] = {
    new ArrayBuffer[Document]()
  }

  def getCategories():ArrayBuffer[Category] = {
    new ArrayBuffer[Category]()
  }
}
