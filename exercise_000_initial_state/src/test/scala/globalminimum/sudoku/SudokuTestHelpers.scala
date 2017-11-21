package globalminimum.sudoku

trait SudokuTestHelpers {

    import ReductionRules.{reductionRuleOne, reductionRuleTwo}

    def stringToReductionSet(stringDef: Vector[String]): ReductionSet = {
      for {
        cellString <- stringDef
      } yield cellString.replaceAll(" ", "").map { _.toString.toInt }.toSet
    }

    def applyReductionRules(reductionSet: ReductionSet): ReductionSet = reductionRuleTwo(reductionRuleOne(reductionSet))

}
