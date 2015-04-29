package models

import com.mongodb.casbah.Imports.ObjectId
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import de.l3s.boilerpipe.extractors.ArticleExtractor

/**
 * Web node html content
 * @author duytd
 */
case class Document(_id: ObjectId = new ObjectId, urlId: ObjectId, content:String, tokenized:Boolean = false) {
  def url: Url = {
    Url.findById(this.urlId).get
  }
}
object DocumentDAO extends SalatDAO[Document, ObjectId](
  collection = DB.mongoDB("documents"))

object Document {
  def extractArticle(html:String): String = {
    val extractor = new ArticleExtractor
    extractor.getText(html)
  }
}
