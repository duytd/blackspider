package com.portia.lib
import org.jsoup.Jsoup

/**
 * Created by duytd on 03/04/2015.
 */
object Utils {
  def escapeDoubleQuote(string:String): String = {
    string.replace("\"","\\\"")
  }

  def html2text(html: String):String = {
    return Jsoup.parse(html).text()
  }
}
