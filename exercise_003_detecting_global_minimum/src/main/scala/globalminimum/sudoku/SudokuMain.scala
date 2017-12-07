package globalminimum.sudoku

import java.io.File

import akka.Done
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
    import scala.concurrent.ExecutionContext.Implicits.global

    val result = (sudokuSolver ? SudokuSolver.InitialRowUpdates(rowUpdates)).mapTo[Done]
    result.flatMap(x => Future(println("Done !"))).onComplete( _ => system.terminate())
  }



}
