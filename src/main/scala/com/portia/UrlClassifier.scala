package com.portia

import com.portia.algorithms.NaiveBayesClassifier

/**
 * Created by duytd on 05/06/2015.
 */
object UrlClassifier {
  def main(args:Array[String]): Unit = {
    val url = args(0)
    val classifier = new NaiveBayesClassifier()
    println(classifier.classifyPageByUrl(url).name)
  }
}
