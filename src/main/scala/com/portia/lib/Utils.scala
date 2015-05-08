package com.portia.lib
import org.jsoup.Jsoup

import scala.util.Random

/**
 * Utility methods
 * @author duytd
 */
object Utils {
  /* Escape double quote to avoid BSON object parsing error */
  def escapeDoubleQuote(string:String): String = {
    string.replace("\"","\\\"")
  }

  /* Convert html to text */
  def html2text(html: String):String = {
    return Jsoup.parse(html).text()
  }

  /* Get random number in range of x, y */
  def getRandom(x:Int, y:Int):Int = {
    val r:Random = new Random()
    val start = x
    val end = y
    r.nextInt(end-start) + start
  }

  /* Check whether a string contains array element
  * E.g The string "happy-birthday" contains element in array ("happy", "birth", "day")
  */
  def hasArrayElementInString(string:String, strArray:Array[String]):Boolean = {
    strArray.foreach(el => {
      if (string.contains(el.trim()))
        return true
    })
    false
  }

}
