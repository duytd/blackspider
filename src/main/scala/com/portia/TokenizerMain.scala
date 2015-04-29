import com.mongodb.casbah.commons.MongoDBObject
import models.{DocumentDAO, Document, Tokenizer}

/**
 * Tokenizer main program
 * @author duytd
 */
object TokenizerMain {
  def main(args: Array[String]): Unit = {
    this.start
  }

  def start: Unit = {
    val tokenizer = new Tokenizer(lang="vi")
    while(true){
      val documents = DocumentDAO.find(MongoDBObject("tokenized"->false))
      tokenizer.tokenizeDocs(documents.toArray[Document])
    }
  }
}
