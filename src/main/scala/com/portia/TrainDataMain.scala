package com.portia

import com.portia.algorithms.PageRank
import com.portia.training.TrainData

/**
 * Created by duytd on 29/04/2015.
 */
object TrainDataMain {
  def main(args: Array[String]): Unit = {
    val trainData = new TrainData
    val ranking = new PageRank
    ranking.run
  }
}