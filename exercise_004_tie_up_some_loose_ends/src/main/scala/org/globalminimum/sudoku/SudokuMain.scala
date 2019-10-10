package org.globalminimum.sudoku

import java.io.File

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

object SudokuMain {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("sudoku-solver-system")

    val sudokuSolver = system.actorOf(SudokuSolver.props(), "sudoku-solver")

    implicit val askTimeout: Timeout = 5.seconds
    val log = system.log
    import system.dispatcher

    val results = for {
      sudokuProblem <- args
      _ = log.info(s"Running solver for $sudokuProblem")
      rowUpdates: Seq[SudokuDetailProcessor.RowUpdate] =
        SudokuIO
          .readSudokuFromFile(new File(sudokuProblem))
          .map { case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update) }
        result = (sudokuSolver ? SudokuSolver.InitialRowUpdates(rowUpdates)).mapTo[SudokuSolver.Result]


      _ = result.flatMap { x => log.info(s"Result ~~> ${x.sudoku.mkString("\n   ", "\n   ", "")}"); Future(log.info("Done !")) }
    } yield result
    Future.sequence(results.to(List)).onComplete(_ => system.terminate())

  }



}
