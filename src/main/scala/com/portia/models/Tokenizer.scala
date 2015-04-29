package models

import java.io.StringReader
import java.util
import com.mongodb.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import org.apache.lucene.analysis._
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.util.CharArraySet
import org.apache.lucene.util.Version
import vn.hus.nlp.tokenizer.VietTokenizer
import java.util.ArrayList
import org.jsoup.Jsoup
import scala.io.Source

/**
 * Tokenizer is a component which can extract tokens from a HTML web document
 * @author duytd
 */
class Tokenizer(lang:String = "en") {
  var STOP_WORDS = new CharArraySet(Version.LATEST, 0, true)
  val viStopWordsFile = "/stop_words/vietnamese_stopwords.txt"
  val enStopWordsFile = "/stop_words/english_stopwords.txt"
  val filePath = if (lang == "vi") viStopWordsFile else enStopWordsFile

  // load stop words
  loadStopWords()

  def tokenizeDocs(documents: Array[Document]):Unit = {

    documents.foreach(doc => {
      var tokens = new ArrayList[String]
      tokens = tokenize(Jsoup.parse(doc.content).text())
      //saveTokensToDB(doc, tokens)
      println(tokens.toString)
    })
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

  /* Method to tokenize English document using Standard Analyzer (Lucene) */
  def tokenize(text:String): ArrayList[String] = {
    val analyzer = new StandardAnalyzer(Version.LATEST, STOP_WORDS)
    getTokens(analyzer, text)
  }

  /* Method to tokenize Vietnamese document using vnTokenizer */
  def tokenizeVi(text: String): util.ArrayList[String] = {
    val tokenizer = new VietTokenizer
    var tokens = new ArrayList[String]
    tokenizer.tokenize(text)(0).split(" ").toList.foreach(token => {

      if (!STOP_WORDS.contains(token)) {
        //remove underscore from compound words
        val normalizedToken = token.replace("_", " ")
        tokens.add(normalizedToken)
      }
    })
    tokens
  }

  private def saveTokensToDB(document: Document, tokens:ArrayList[String]): Unit ={
    val docTokensObj = new DocTokens(documentId = document._id, tokens = tokens)
    DocTokensDAO.insert(docTokensObj)
    println("Tokenized " + document.url.absPath)

    // Mark document as tokenized
    val updatedDoc = document.copy(tokenized = true)
    DocumentDAO.update(MongoDBObject("_id"->document._id), updatedDoc, upsert = false, multi = false, new WriteConcern)
  }

  private def getTokens(analyzer: Analyzer, document: String): ArrayList[String] = {
    val stream  = analyzer.tokenStream(null, new StringReader(document))
    val tokens = new ArrayList[String]
    try {
      stream.reset()
      while(stream.incrementToken()) {
        tokens.add(stream.getAttribute(classOf[CharTermAttribute]).toString)
      }
    }
    catch {
      case e: Exception => println("Exception caught: " + e.getMessage)
    }
    tokens
  }
}
