package globalminimum.sudoku

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import globalminimum.sudoku.SudokuDetailProcessor.UpdateSender

object SudokuDetailProcessor {

  case class Update(cellUpdates: CellUpdates)
  case class RowUpdate(id: Int, cellUpdates: CellUpdates)
  case class ColumnUpdate(id: Int, cellUpdates: CellUpdates)
  case class BlockUpdate(id: Int, cellUpdates: CellUpdates)
  case object SudokuDetailUnchanged

  val InitialDetailState: ReductionSet = CELLIndexesVector.map(_ => InitialCell)

  def props[DetailType <: SudokoDetailType](id: Int, state: ReductionSet = InitialDetailState)(implicit updateSender: UpdateSender[DetailType]): Props =
    Props(new SudokuDetailProcessor[DetailType](id, state)(updateSender))

  trait UpdateSender[A] {
    def sendUpdate(id: Int, cellUpdates: CellUpdates)(implicit sender: ActorRef): Unit
  }

  implicit val rowUpdateSender: UpdateSender[Row] = new UpdateSender[Row] {
    override def sendUpdate(id: Int, cellUpdates: CellUpdates)(implicit sender: ActorRef): Unit = {
//      println(s"sending a row update... to $sender")
      sender ! RowUpdate(id, cellUpdates)
    }
  }

  implicit val columnUpdateSender: UpdateSender[Column] = new UpdateSender[Column] {
    override def sendUpdate(id: Int, cellUpdates: CellUpdates)(implicit sender: ActorRef): Unit =
      sender ! ColumnUpdate(id, cellUpdates)
  }

  implicit val blockUpdateSender: UpdateSender[Block] = new UpdateSender[Block] {
    override def sendUpdate(id: Int, cellUpdates: CellUpdates)(implicit sender: ActorRef): Unit =
      sender ! BlockUpdate(id, cellUpdates)
  }
}

class SudokuDetailProcessor[DetailType <: SudokoDetailType : UpdateSender](id: Int, state: ReductionSet) extends Actor with ActorLogging {

  import SudokuDetailProcessor._
  import ReductionRules.{reductionRuleOne, reductionRuleTwo}

  override def receive: Receive = operational(id, state)

  def operational(id: Int, state: ReductionSet = InitialDetailState): Receive = {

    case Update(cellUpdates) =>
      val updatedState = mergeState(state, cellUpdates)
      val transformedUpdatedState = reductionRuleTwo(reductionRuleOne(updatedState))
      if (transformedUpdatedState == state) {
        sender ! SudokuDetailUnchanged
      } else {
//        println(s"Going to send an update. Sender = $sender")
        val updateSender = implicitly[UpdateSender[DetailType]]
        updateSender.sendUpdate(id, stateChanges(state, transformedUpdatedState))(sender)
        //sender ! Update(stateChanges(state, transformedUpdatedState))
        context.become(operational(id, transformedUpdatedState))
      }
  }


  
  private def mergeState(state: ReductionSet, cellUpdates: CellUpdates): ReductionSet = {
    (cellUpdates foldLeft state) {
      case (stateTally, (index, updatedCellContent)) =>
        stateTally.updated(index, stateTally(index) & updatedCellContent)
    }
  }

  private def stateChanges(state: ReductionSet, updatedState: ReductionSet): CellUpdates = {
    ((state zip updatedState).zipWithIndex foldRight cellUpdatesEmpty) {
      case (((previousCellContent, updatedCellContent), index), cellUpdates)
        if updatedCellContent != previousCellContent =>
          (index, updatedCellContent) +: cellUpdates

      case (_, cellUpdates) => cellUpdates
    }
  }
}
