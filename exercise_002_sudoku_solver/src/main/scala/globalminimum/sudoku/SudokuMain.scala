package globalminimum.sudoku

import java.io.File

import akka.actor.ActorSystem

object SudokuMain {

  def main(args: Array[String]): Unit = {

    val rowUpdates =
      SudokuIO.readSudokuFromFile(new File(args(0)))
      .map{ case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update)}

    val system = ActorSystem("sudoku")

    val sudokuSolver = system.actorOf(SudokuSolver.props())

    rowUpdates.foreach(update => sudokuSolver ! update)
    Thread.sleep(1000)
    sudokuSolver ! SudokuDetailProcessor.PrintResult
    Thread.sleep(200)
    system.terminate()
  }



}
