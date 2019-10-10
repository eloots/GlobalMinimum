package org.globalminimum.sudoku

import java.io.File

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

object SudokuMain {

  def main(args: Array[String]): Unit = {

    val rowUpdates: Seq[SudokuDetailProcessor.RowUpdate] =
      SudokuIO.readSudokuFromFile(new File(args(0)))
      .map{ case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update)}

    val system = ActorSystem("sudoku")

    val sudokuSolver = system.actorOf(SudokuSolver.props())

    implicit val askTimeout: Timeout = 5.seconds
    import system.dispatcher

    val result = (sudokuSolver ? SudokuSolver.InitialRowUpdates(rowUpdates)).mapTo[SudokuSolver.Result]
    result.flatMap{x => println(s"Result ~~> ${x.sudoku.mkString("\n   ", "\n   ", "")}"); Future(println("Done !"))}.onComplete( _ => system.terminate())
  }



}
