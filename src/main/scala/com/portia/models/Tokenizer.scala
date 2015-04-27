package models

import java.io.StringReader
import java.util
import com.mongodb.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import org.apache.lucene.analysis._
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import vn.hus.nlp.tokenizer.VietTokenizer
import java.util.ArrayList
import org.jsoup.Jsoup
/**
 * Tokenizer is a component which can extract tokens from a HTML web document
 * @author duytd
 */
class Tokenizer {

  def tokenizeDocs(documents: Array[Document]):Unit = {

    documents.foreach(doc => {
      var tokens = new ArrayList[String]
      tokens = tokenize(Jsoup.parse(doc.content).text())
      saveTokensToDB(doc, tokens)
    })
  }

  /* Method to tokenize English document using Standard Analyzer (Lucene) */
  private def tokenize(text:String): ArrayList[String] = {
    val analyzer = new StandardAnalyzer()
    getTokens(analyzer, text)
  }

  /* Method to tokenize Vietnamese document using vnTokenizer */
  private def tokenizeVi(text: String): util.ArrayList[String] = {
    val tokenizer = new VietTokenizer
    var tokens = new ArrayList[String]
    tokenizer.tokenize(text)(0).split(" ").toList.foreach(token => {
      //remove underscore from compound words
      val normalizedToken = token.replace("_", " ")
      tokens.add(normalizedToken)
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
