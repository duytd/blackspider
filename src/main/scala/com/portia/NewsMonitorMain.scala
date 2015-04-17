/**
 * News monitor main program
 * @author duytd
 */
object NewsMonitorMain {
  def main(args: Array[String]): Unit = {
    val targetUrls = Constants.TARGET_URLS
    targetUrls.foreach(url => {
      val newsMonitor = new NewsMonitor(url._2, url._1)
      newsMonitor.monitor()
    })
  }
}
