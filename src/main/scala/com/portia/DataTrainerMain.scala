package com.portia

import com.portia.trainer.DataTrainer

/**
 * Data trainer main program
 * @author duytd
 */
object DataTrainerMain {
  def main(args: Array[String]): Unit = {
    val dataTrainer = new DataTrainer
    dataTrainer.run()
  }
}
