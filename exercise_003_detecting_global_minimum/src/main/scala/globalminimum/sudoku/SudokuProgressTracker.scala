package globalminimum.sudoku

import akka.actor.{Actor, ActorLogging, Props}

object SudokuProgressTracker {

  case class NewUpdatesInFlight(count: Int)

  def props(): Props = Props(new SudokuProgressTracker)

}

class SudokuProgressTracker extends Actor with ActorLogging {

  import SudokuProgressTracker._

  override def receive: Receive = trackProgress(updatesInFlight = 0)

  def trackProgress(updatesInFlight: Int) : Receive = {
    case NewUpdatesInFlight(updateCount) =>
      context.become(trackProgress(updatesInFlight + updateCount))

    case SudokuDetailProcessor.SudokuDetailUnchanged if updatesInFlight - 1 == 0 =>
      context.parent ! SudokuDetailProcessor.PrintResult
      context.become(trackProgress(0))

    case SudokuDetailProcessor.SudokuDetailUnchanged =>
      context.become(trackProgress(updatesInFlight - 1))
  }
}
