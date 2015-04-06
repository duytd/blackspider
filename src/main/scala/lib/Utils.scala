package lib

/**
 * Created by duytd on 03/04/2015.
 */
object Utils {
  def escapeDoubleQuote(string:String): String = {
    string.replace("\"","\\\"")
  }
}
