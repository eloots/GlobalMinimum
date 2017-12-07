package globalminimum.sudoku

import java.io.{FileReader, BufferedReader, File}


object SudokuIO {

  def printRow( row: ReductionSet): String = {
    def printSubRow( subRowNo: Int): String = {
      val printItems = List(1,2,3) map( x => x + subRowNo * 3)
      (for { elem <- row }
        yield {
          (printItems map (item => if ((elem & printItems.toSet).contains(item)) item.toString else " ")).mkString("")
        }).mkString("| ", " | ", " |")
    }
    (for { subRow <- 0 until 3 } yield printSubRow(subRow)).mkString("\n")
  }

//  def printField( field: SudokuField): Unit = {
//    val sepLine = "+-----"*(9)+"+"
//    println(sepLine)
//    for (row <- 0 until 9) {
//      println(printRow(field.cells(row)))
//      println(sepLine)
//    }
//  }

  def printRowShort( row: ReductionSet): String = {
    (for {
      elem <- row
    } yield {
      if (elem.size == 1) elem.head.toString else " "
    }).mkString("|","|","|")

  }

//  def printFieldShort( field: SudokuField): Unit = {
//    val sepLine = "+-"*(9)+"+"
//    println(sepLine)
//    for (row <- 0 until 9) {
//      println(printRowShort(field.cells(row)))
//      println(sepLine)
//    }
//  }

  /*
   * FileLineTraversable code taken from "Scala in Depth" by Joshua Suereth
   */

  import scala.language.postfixOps
  import java.io.BufferedReader
  import java.io.FileReader
  import java.io.File
  class FileLineTraversable(file: File) extends Traversable[String] {
    override def foreach[U](f: String => U): Unit = {
      val input = new BufferedReader(new FileReader(file))
      try {
        var line = input.readLine
        while (line != null) {
          f(line)
          line = input.readLine
        }
      } finally {
        input.close()
      }
    }

    override def toString: String =
      "{Lines of " + file.getAbsolutePath + "}"
  }

  def readSudokuFromFile(sudokuInputFile: java.io.File): Seq[(Int, CellUpdates)] = {
    val dataLines = new FileLineTraversable(sudokuInputFile).toList
    val cellsIn =
      dataLines
        .map { inputLine => """\|""".r replaceAllIn(inputLine, "")}     // Remove 3x3 separator character
        .filter (_ != "---+---+---")              // Remove 3x3 line separator
        .map ("""^[1-9 ]{9}$""".r findFirstIn(_)) // Input data should only contain values 1-9 or ' '
        .collect { case Some(x) => x}
        .zipWithIndex
    var modCells = Seq.empty[(Int,Int)]
    for {
      (rowCells, row) <- cellsIn
      updates = (rowCells.zipWithIndex foldLeft cellUpdatesEmpty) {
        case (cellUpdates, (c, index)) if c != ' ' =>
          (index, Set(c.toString.toInt)) +: cellUpdates
        case (cellUpdates, _) => cellUpdates
      }

    } yield (row, updates)
  }
}
