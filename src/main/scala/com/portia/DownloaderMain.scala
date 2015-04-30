import com.portia.models.{Document, Downloader}
import org.jsoup.Jsoup

/**
 * Downloader main program
 * @author duytd
 */
object DownloaderMain {
  def main(args: Array[String]): Unit = {
    var downloader = new Downloader
    downloader.run
  }
}
