package globalminimum.sudoku

import akka.Done
import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Props}
import globalminimum.sudoku.SudokuDetailProcessor.UpdateSender

object SudokuSolver {

  case class SetInitialCells(updates: CellUpdates)

  case class InitialRowUpdates(rowUpdates: Seq[SudokuDetailProcessor.RowUpdate])

  def genDetailProcessors[A <: SudokoDetailType : UpdateSender](context: ActorContext): Map[Int, ActorRef] = {
    cellIndexesVector.map {
      case index =>
        val detailProcessor = context.actorOf(SudokuDetailProcessor.props[A](index))
        (index, detailProcessor)
    }.toMap
  }

  def props(): Props = Props(new SudokuSolver)
}

class SudokuSolver extends Actor with ActorLogging {

  import SudokuSolver._

  private val rowDetailProcessors    = genDetailProcessors[Row](context)
  private val columnDetailProcessors = genDetailProcessors[Column](context)
  private val blockDetailProcessors  = genDetailProcessors[Block](context)

  private val progressTracker = context.actorOf(SudokuProgressTracker.props(), "sudoku-progress-tracker")

  import CellMappings._

  override def receive: Receive = processRequest(None)

  def processRequest(requestor: Option[ActorRef]): Receive = {
    case SudokuDetailProcessor.RowUpdate(rowNr, updates) =>
//      log.debug(s"Detail Processor: Rowupdate(${rowNr}/${updates.size})")
      updates.foreach {
        case (rowCellNr, newCellContent) =>

          val (columnNr, columnCellNr) = rowToColumnCoordinates(rowNr, rowCellNr)
          val columnUpdate = List((columnCellNr, newCellContent))
          columnDetailProcessors(columnNr) ! SudokuDetailProcessor.Update(columnUpdate)

          val (blockNr, blockCellNr) = rowToBlockCoordinates(rowNr, rowCellNr)
          val blockUpdate = List((blockCellNr, newCellContent))
          blockDetailProcessors(blockNr) ! SudokuDetailProcessor.Update(blockUpdate)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)

    case SudokuDetailProcessor.ColumnUpdate(columnNr, updates) =>
//      log.debug(s"Detail Processor: Columnupdate(${columnNr}/${updates.size})")
      updates.foreach {
        case (colCellNr, newCellContent) =>

          val (rowNr, rowCellNr) = columnToRowCoordinates(columnNr, colCellNr)
          val rowUpdate = List((rowCellNr, newCellContent))
          rowDetailProcessors(rowNr) ! SudokuDetailProcessor.Update(rowUpdate)

          val (blockNr, blockCellNr) = columnToBlockCoordinates(columnNr, colCellNr)
          val blockUpdate = List((blockCellNr, newCellContent))
          blockDetailProcessors(blockNr) ! SudokuDetailProcessor.Update(blockUpdate)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)

    case SudokuDetailProcessor.BlockUpdate(blockNr, updates) =>
//      log.debug(s"Detail Processor: Blockupdate(${blockNr}/${updates.size})")
      updates.foreach {
        case (blockCellNr, newCellContent) =>

          val (rowNr, rowCellNr) = blockToRowCoordinates(blockNr, blockCellNr)
          val rowUpdate = List((rowCellNr, newCellContent))
          rowDetailProcessors(rowNr) ! SudokuDetailProcessor.Update(rowUpdate)

          val (columnNr, columnCellNr) = blockToColumnCoordinates(blockNr, blockCellNr)
          val columnUpdate = List((columnCellNr, newCellContent))
          columnDetailProcessors(columnNr) ! SudokuDetailProcessor.Update(columnUpdate)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)

    case InitialRowUpdates(rowUpdates) =>
      rowUpdates.foreach {
        case SudokuDetailProcessor.RowUpdate(row, cellUpdates) =>
          rowDetailProcessors(row) ! SudokuDetailProcessor.Update(cellUpdates)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(rowUpdates.size)
      context.become(processRequest(Some(sender())))


    case unchanged @ SudokuDetailProcessor.SudokuDetailUnchanged =>
//      log.debug(s"SudokuDetailUnchanged")
      progressTracker ! unchanged

    case SudokuDetailProcessor.PrintResult =>
      rowDetailProcessors.foreach { case (_, processor) => processor ! SudokuDetailProcessor.PrintResult }
      requestor.get ! Done
      context.become(processRequest(None))
  }
}