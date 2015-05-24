package com.portia
import com.portia.trainer.DataTrainer

/**
 * Data trainer main program
 * @author duytd
 */
object DataTrainerMain {
  def main(args: Array[String]): Unit = {
    //Training Vietnamese Set
    val dataTrainer = new DataTrainer(lang = "vi")
    dataTrainer.run()
  }
}
