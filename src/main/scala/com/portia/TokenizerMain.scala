import com.mongodb.casbah.commons.MongoDBObject
import models.{DocumentDAO, Document, UrlDAO, Tokenizer}

/**
 * Tokenizer main program
 * @author duytd
 */
object TokenizerMain {
  def main(args: Array[String]): Unit = {
    this.start
  }

  def start: Unit = {
    while(true){
      val documents = DocumentDAO.find(MongoDBObject("tokenized"->false))
      val tokenizer = new Tokenizer
      tokenizer.tokenizeDocs(documents.toArray[Document])
    }
  }
}
