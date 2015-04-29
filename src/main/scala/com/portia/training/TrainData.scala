package com.portia.training

import com.portia.algorithms.NaiveBayesClassifier

import scala.collection.mutable.ArrayBuffer

/**
 * @author qmha
 */
class TrainData {
  // Update categoryID for Document
  def updateCategory(): Unit = {

  }

  def train(): Unit = {
    var nbc:NaiveBayesClassifier = new NaiveBayesClassifier
    nbc.learnNaiveBayesText
  }
}
