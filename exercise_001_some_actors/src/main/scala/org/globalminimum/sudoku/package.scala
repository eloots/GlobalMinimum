package org.globalminimum

package object sudoku {

  sealed trait SudokoDetailType
  case class Row(id: Int) extends SudokoDetailType
  case class Column(id: Int) extends SudokoDetailType
  case class Block(id: Int) extends SudokoDetailType

  type Seq[+A] = scala.collection.immutable.Seq[A]
  val Seq = scala.collection.immutable.Seq

  private val N = 9
  val CELLPossibleValues: Vector[Int] = (1 to N).toVector
  val CELLIndexesVector: Vector[Int] = (0 until N).toVector
  val InitialCell: Set[Int] = Set(1 to N: _*)

  type CellContent = Set[Int]
  type ReductionSet = Vector[CellContent]

  type CellUpdates = Seq[(Int, Set[Int])]
  val cellUpdatesEmpty = Seq.empty[(Int, Set[Int])]

  object ReductionRules {

    def reductionRuleOne(reductionSet: ReductionSet): ReductionSet = {
      val inputCellsGrouped = reductionSet filter {_.size <= 7} groupBy identity
      val completeInputCellGroups = inputCellsGrouped filter {
        case (set, setOccurrences) => set.size == setOccurrences.length
      }
      val completeAndIsolatedValueSets = completeInputCellGroups.keys.toList
      (completeAndIsolatedValueSets foldLeft reductionSet) {
        case (cells, caivSet) => cells map {
          cell => if (cell != caivSet) cell &~ caivSet else cell
        }
      }
    }

    def reductionRuleTwo(reductionSet: ReductionSet): ReductionSet = {
      val valueOccurrences = CELLPossibleValues map { value =>
        (CELLIndexesVector zip reductionSet foldLeft Vector.empty[Int]) {
          case (acc, (index, cell)) =>
            if (cell contains value) index +: acc else acc
        }
      }

      val cellIndexesToValues =
        (CELLPossibleValues zip valueOccurrences)
          .groupBy { case (value, occurrence) => occurrence}
          .filter  { case (loc, occ) => loc.length == occ.length && loc.length <= 6 }

      val cellIndexListToReducedValue = cellIndexesToValues map {
        case (index, seq) => (index, (seq map { case (value, _) => value }).toSet)
      }

      val cellIndexToReducedValue = cellIndexListToReducedValue flatMap {
        case (cellIndexList, reducedValue) =>
          cellIndexList map { cellIndex => cellIndex -> reducedValue }
      }

      (reductionSet.zipWithIndex foldRight Vector.empty[CellContent]) {
        case ((cellValue, cellIndex), acc) =>
          cellIndexToReducedValue.getOrElse(cellIndex, cellValue) +: acc
      }
    }
  }

}
