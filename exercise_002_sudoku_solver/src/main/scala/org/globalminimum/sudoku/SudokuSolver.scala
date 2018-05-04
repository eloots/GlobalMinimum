package org.globalminimum.sudoku

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Props}
import org.globalminimum.sudoku.SudokuDetailProcessor.UpdateSender

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

  import CellMappings._
  override def receive: Receive = {
    case SudokuDetailProcessor.RowUpdate(rowNr, updates) =>
      updates.foreach {
        case (rowCellNr, newCellContent) =>

          val (columnNr, columnCellNr) = rowToColumnCoordinates(rowNr, rowCellNr)
          val columnUpdate = List((columnCellNr, newCellContent))
          columnDetailProcessors(columnNr) ! SudokuDetailProcessor.Update(columnUpdate)

          val (blockNr, blockCellNr) = rowToBlockCoordinates(rowNr, rowCellNr)
          val blockUpdate = List((blockCellNr, newCellContent))
          blockDetailProcessors(blockNr) ! SudokuDetailProcessor.Update(blockUpdate)
      }

    case SudokuDetailProcessor.ColumnUpdate(columnNr, updates) =>
      updates.foreach {
        case (colCellNr, newCellContent) =>

          val (rowNr, rowCellNr) = columnToRowCoordinates(columnNr, colCellNr)
          val rowUpdate = List((rowCellNr, newCellContent))
          rowDetailProcessors(rowNr) ! SudokuDetailProcessor.Update(rowUpdate)

          val (blockNr, blockCellNr) = columnToBlockCoordinates(columnNr, colCellNr)
          val blockUpdate = List((blockCellNr, newCellContent))
          blockDetailProcessors(blockNr) ! SudokuDetailProcessor.Update(blockUpdate)
      }

    case SudokuDetailProcessor.BlockUpdate(blockNr, updates) =>
      updates.foreach {
        case (blockCellNr, newCellContent) =>

          val (rowNr, rowCellNr) = blockToRowCoordinates(blockNr, blockCellNr)
          val rowUpdate = List((rowCellNr, newCellContent))
          rowDetailProcessors(rowNr) ! SudokuDetailProcessor.Update(rowUpdate)

          val (columnNr, columnCellNr) = blockToColumnCoordinates(blockNr, blockCellNr)
          val columnUpdate = List((columnCellNr, newCellContent))
          columnDetailProcessors(columnNr) ! SudokuDetailProcessor.Update(columnUpdate)
      }

    case InitialRowUpdates(rowUpdates) =>
      rowUpdates.foreach {
        case SudokuDetailProcessor.RowUpdate(row, cellUpdates) =>
          rowDetailProcessors(row) ! SudokuDetailProcessor.Update(cellUpdates)
      }

    case SudokuDetailProcessor.SudokuDetailUnchanged =>

    case SudokuDetailProcessor.PrintResult =>
      rowDetailProcessors.foreach { case (_, processor) => processor ! SudokuDetailProcessor.PrintResult }
  }
}