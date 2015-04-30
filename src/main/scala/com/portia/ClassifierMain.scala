package com.portia

import com.portia.algorithms.NaiveBayesClassifier

/**
 * Classifier main program
 * @author duytd
 */
object ClassifierMain {
  def main(args:Array[String]): Unit ={
    val nbc = new NaiveBayesClassifier
    val testUrl = "http://dantri.com.vn/su-kien/hlv-miura-cong-bo-18-cau-thu-tap-trung-doi-tuyen-quoc-gia-1065884.htm"
    nbc.classifyPageByUrl(testUrl)
  }
}
