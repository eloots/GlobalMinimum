package org.globalminimum.sudoku

import java.io.File

import akka.actor.ActorSystem

object SudokuMain {

  def main(args: Array[String]): Unit = {

    val rowUpdates: Seq[SudokuDetailProcessor.RowUpdate] =
      SudokuIO.readSudokuFromFile(new File(args(0)))
      .map{ case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update)}

    val system = ActorSystem("sudoku")

    val sudokuSolver = system.actorOf(SudokuSolver.props())

    sudokuSolver ! SudokuSolver.InitialRowUpdates(rowUpdates)
    //rowUpdates.foreach(update => sudokuSolver ! update)
    Thread.sleep(1000)
    sudokuSolver ! SudokuDetailProcessor.PrintResult
    Thread.sleep(200)
    system.terminate()
  }



}
