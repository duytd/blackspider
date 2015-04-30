package com.portia

import com.portia.algorithms.PageRank

/**
 * Ranker main program
 * @author duytd
 */
object RankerMain {
  def main(args:Array[String]): Unit = {
    val ranker = new PageRank
    ranker.run()
  }
}
