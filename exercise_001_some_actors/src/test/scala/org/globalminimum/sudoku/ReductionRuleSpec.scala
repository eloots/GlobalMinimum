/**
 * Copyright © 2014 - 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package org.globalminimum.sudoku

import org.scalatest.{Matchers, WordSpec}

class ReductionRuleSpec extends WordSpec with Matchers with SudokuTestHelpers {
  "Applying reduction rules" should {
    "Eliminate values in isolated complete sets from occurrences in other cells (First reduction rule)" in {
      val input = stringToReductionSet(Vector(
        "12345678 ",
        "1        ", // 1: Isolated & complete
        "   4     ", // 4: Isolated & complete
        "12 45678 ",
        "      78 ", // (7,8): Isolated & complete
        "       89",
        "      78 ", // (7,8): Isolated & complete
        "     6789",
        " 23   78 "
      ))

      val reducedInput1 = stringToReductionSet(Vector(
        " 23 56   ",
        "1        ",
        "   4     ",
        " 2  56   ",
        "      78 ",
        "        9",
        "      78 ",
        "     6  9",
        " 23      "
      ))

      applyReductionRules(input) shouldEqual reducedInput1

      // After first reduction round, 9 is isolated & complete
      val reducedInput2 = stringToReductionSet(Vector(
        " 23 56   ",
        "1        ",
        "   4     ",
        " 2  56   ",
        "      78 ",
        "        9",
        "      78 ",
        "     6   ",
        " 23      "
      ))

      applyReductionRules(reducedInput1) shouldEqual reducedInput2

      // After second reduction round, 6 is isolated & complete
      val reducedInput3 = stringToReductionSet(Vector(
        " 23 5    ",
        "1        ",
        "   4     ",
        " 2  5    ",
        "      78 ",
        "        9",
        "      78 ",
        "     6   ",
        " 23      "
      ))

      applyReductionRules(reducedInput2) shouldEqual reducedInput3

      // reducing again should give same result
      applyReductionRules(reducedInput3) shouldEqual reducedInput3
    }

    "Eliminate values in isolated complete sets of 5 values from occurrences in other cells (First reduction rule)" in {
      val input = stringToReductionSet(Vector(
        "12345    ", // (1,2,3,4,5): Isolated & complete
        "1    67  ",
        "12345    ", // (1,2,3,4,5): Isolated & complete
        "12345    ", // (1,2,3,4,5): Isolated & complete
        " 2    78 ",
        "12345    ", // (1,2,3,4,5): Isolated & complete
        "  3    89",
        "12345    ", // (1,2,3,4,5): Isolated & complete
        "1 3  6  9"
      ))

      val reducedInput = stringToReductionSet(Vector(
        "12345    ",
        "     67  ",
        "12345    ",
        "12345    ",
        "      78 ",
        "12345    ",
        "       89",
        "12345    ",
        "     6  9"
      ))

      applyReductionRules(input) shouldEqual reducedInput

    }

    "Eliminate values in 2 isolated complete sets of 3 values from occurrences in other cells (First reduction rule)" in {
      val input = stringToReductionSet(Vector(
        "123      ",
        "123      ",
        "123      ",
        "   456   ",
        "   456   ",
        "   456   ",
        "12345678 ",
        "123456 89",
        "1234567 9"
      ))

      val reducedInput = stringToReductionSet(Vector(
        "123      ",
        "123      ",
        "123      ",
        "   456   ",
        "   456   ",
        "   456   ",
        "      78 ",
        "       89",
        "      7 9"
      ))

      applyReductionRules(input) shouldEqual reducedInput
    }

    "Eliminate values in shadowed complete sets from occurrences in same cells (Second reduction rule)" in {
      val input = stringToReductionSet(Vector(
        "12  5 789", // (1,2,7,8) shadowed & complete
        "  345    ", // (3,4)     shadowed & complete
        "12  5678 ", // (1,2,7,8) shadowed & complete
        "    56  9",
        "    56  9",
        "12    789", // (1,2,7,8) shadowed & complete
        "  3456  9", // (3,4)     shadowed & complete
        "12   6789", // (1,2,7,8) shadowed & complete
        "        9"
      ))

      val reducedInput1 = stringToReductionSet(Vector(
        "12    78 ",
        "  34     ",
        "12    78 ",
        "    56   ",
        "    56   ",
        "12    78 ",
        "  34     ",
        "12    78 ",
        "        9"
      ))

      applyReductionRules(input) shouldEqual reducedInput1

      // reducing again gives same result
      applyReductionRules(reducedInput1) shouldEqual reducedInput1
    }

    "Eliminate values in shadowed complete (6 value) sets from occurrences in same cells (Second reduction rule)" in {
      val input = stringToReductionSet(Vector(
        "123456 89", // (1,2,3,4,5,6) shadowed & complete
        "      78 ",
        "12345678 ", // (1,2,3,4,5,6) shadowed & complete
        "123456789", // (1,2,3,4,5,6) shadowed & complete
        "123456 8 ", // (1,2,3,4,5,6) shadowed & complete
        "       89",
        "123456 8 ", // (1,2,3,4,5,6) shadowed & complete
        "      7 9",
        "12345678 "  // (1,2,3,4,5,6) shadowed & complete
      ))

      val reducedInput = stringToReductionSet(Vector(
        "123456   ",
        "      78 ",
        "123456   ",
        "123456   ",
        "123456   ",
        "       89",
        "123456   ",
        "      7 9",
        "123456   "
      ))

      applyReductionRules(input) shouldEqual reducedInput
    }
  }
}
