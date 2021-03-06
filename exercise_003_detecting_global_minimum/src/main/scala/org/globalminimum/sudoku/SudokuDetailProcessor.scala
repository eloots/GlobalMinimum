package org.globalminimum.sudoku

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.globalminimum.sudoku.SudokuDetailProcessor.UpdateSender

object SudokuDetailProcessor {

  case class Update(cellUpdates: CellUpdates)
  case class RowUpdate(rowId: Int, cellUpdates: CellUpdates)
  case class ColumnUpdate(columnId: Int, cellUpdates: CellUpdates)
  case class BlockUpdate(blockId: Int, cellUpdates: CellUpdates)
  case object SudokuDetailUnchanged
  case object GetSudokuDetailState
  case class  SudokuDetailState(index: Int, state: ReductionSet)

  val InitialDetailState: ReductionSet = cellIndexesVector.map(_ => initialCell)

  def props[DetailType <: SudokoDetailType](id: Int, state: ReductionSet = InitialDetailState)(implicit updateSender: UpdateSender[DetailType]): Props =
    Props(new SudokuDetailProcessor[DetailType](id, state)(updateSender))

  trait UpdateSender[A] {
    def sendUpdate(id: Int, cellUpdates: CellUpdates)(implicit sender: ActorRef): Unit
    def processorType: String
  }

  implicit val rowUpdateSender: UpdateSender[Row] = new UpdateSender[Row] {
    override def sendUpdate(id: Int, cellUpdates: CellUpdates)(implicit sender: ActorRef): Unit =
      sender ! RowUpdate(id, cellUpdates)
    override def processorType: String = "row"
  }

  implicit val columnUpdateSender: UpdateSender[Column] = new UpdateSender[Column] {
    override def sendUpdate(id: Int, cellUpdates: CellUpdates)(implicit sender: ActorRef): Unit =
      sender ! ColumnUpdate(id, cellUpdates)
    override def processorType: String = "column"
  }

  implicit val blockUpdateSender: UpdateSender[Block] = new UpdateSender[Block] {
    override def sendUpdate(id: Int, cellUpdates: CellUpdates)(implicit sender: ActorRef): Unit =
      sender ! BlockUpdate(id, cellUpdates)
    override def processorType: String = "block"
  }
}

class SudokuDetailProcessor[DetailType <: SudokoDetailType : UpdateSender](id: Int, state: ReductionSet) extends Actor with ActorLogging {

  import SudokuDetailProcessor._
  import ReductionRules.{reductionRuleOne, reductionRuleTwo}

  override def receive: Receive = operational(id, state)

  def operational(id: Int, state: ReductionSet = InitialDetailState, fullyReduced: Boolean = false): Receive = {

    case Update(cellUpdates) if ! fullyReduced =>
      val updatedState = mergeState(state, cellUpdates)
      val transformedUpdatedState = reductionRuleTwo(reductionRuleOne(updatedState))
      if (transformedUpdatedState == state) {
        sender ! SudokuDetailUnchanged
      } else {
        val updateSender = implicitly[UpdateSender[DetailType]]
        updateSender.sendUpdate(id, stateChanges(state, transformedUpdatedState))(sender)
        context.become(operational(id, transformedUpdatedState, isFullyReduced(transformedUpdatedState)))
      }

    case Update(cellUpdates) =>
      //      log.debug(s"State: Nothing left to do ! $state")
      sender ! SudokuDetailUnchanged

    case GetSudokuDetailState =>
      sender() ! SudokuDetailState(id, state)
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

  private def isFullyReduced(state: ReductionSet): Boolean = {
    val allValuesInState = state.flatten
    allValuesInState == allValuesInState.distinct
  }
}
