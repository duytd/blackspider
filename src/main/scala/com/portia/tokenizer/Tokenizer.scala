package com.portia.tokenizer

import java.io.StringReader
import com.mongodb.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import com.portia.analyzer.PortiaAnalyzer
import org.apache.lucene.analysis.{CharArraySet,Analyzer}
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.Version
import com.portia.models._
import com.portia.lib.Utils
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
 * Tokenizer is a component which can extract tokens from a HTML web document
 * @author duytd
 */
class Tokenizer(lang:String = "en") {
  var STOP_WORDS = new CharArraySet(Version.LUCENE_30, 0, true)
  val viStopWordsFile = "/stop_words/vietnamese_stopwords.txt"
  val enStopWordsFile = "/stop_words/english_stopwords.txt"
  val filePath = if (lang == "vi") viStopWordsFile else enStopWordsFile

  // load stop words
  loadStopWords()

  def tokenizeDoc(document: Document):Unit = {
    var tokens = new ArrayBuffer[String]
    tokens = tokenize(Utils.html2text(document.content))
    saveTokensToDB(document, tokens)
  }

  def tokenizeMultiDocs(documents: Array[Document]):Unit = {
    var count = 0
    val d_size = documents.length
    println("Tokenizing docs...")
    documents.foreach(doc => {
      tokenizeDoc(doc)
      count = count + 1
      print("Processing: "+ count+"/"+d_size+"\r")
    })
    println("\nFinish tokenizing docs...")
  }

  private def loadStopWords(): Unit ={
    for (word <- Source.fromURL(getClass.getResource(this.filePath), "UTF-8").getLines()) {
      if (this.lang == "vi") {
        STOP_WORDS.add(word)
      }
      else {
        STOP_WORDS.add(word)
      }
    }
  }

  /* Method to tokenize English document using Portia Analyzer */
  def tokenize(text:String): ArrayBuffer[String] = {
    val analyzer = new PortiaAnalyzer(STOP_WORDS)
    getTokens(analyzer, text)
  }

  private def saveTokensToDB(document: Document, tokenNames:ArrayBuffer[String]): Unit ={
    tokenNames.par.foreach(tokenName => {
      if (!Token.existedToken(tokenName.toString)) {
        val tokenObj = new Token(name = tokenName.toString)
        TokenDAO.insert(tokenObj)
      }
    })

    // Mark document as tokenized
    val updatedDoc = document.copy(tokenized = true)
    DocumentDAO.update(MongoDBObject("_id"->document._id), updatedDoc, upsert = false, multi = false, new WriteConcern)
  }

  private def getTokens(analyzer: Analyzer, document: String): ArrayBuffer[String] = {
    val stream  = analyzer.tokenStream(null, new StringReader(document))
    val tokens = new ArrayBuffer[String]()
    try {
      stream.reset()
      while(stream.incrementToken()) {
        tokens += stream.getAttribute(classOf[CharTermAttribute]).toString
      }
    }
    catch {
      case e: Exception => println("Exception caught: " + e.getMessage)
    }
    tokens
  }
}
