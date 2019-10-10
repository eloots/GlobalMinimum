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

    // Send initial state of Sudoku rows to SudokuSolver
    sudokuSolver ! SudokuSolver.InitialRowUpdates(rowUpdates)
    // Let the SudokuSolver to its work for 1000ms...
    Thread.sleep(1000)
    // Request printing of the state of the rows - ordering of printed rows is non-deterministic
    sudokuSolver ! SudokuDetailProcessor.PrintResult
    Thread.sleep(200)
    system.terminate()
  }



}
