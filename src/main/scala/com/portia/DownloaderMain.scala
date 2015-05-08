package com.portia
import com.portia.models.{Downloader}

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
