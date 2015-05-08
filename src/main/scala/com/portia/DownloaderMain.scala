package com.portia
import com.portia.downloader.Downloader

/**
 * Downloader main program
 * @author duytd
 */
object DownloaderMain {
  def main(args: Array[String]): Unit = {
    val downloader = new Downloader
    downloader.run
  }
}
