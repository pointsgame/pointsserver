package net.pointsgame.paper_engine

import org.scalatest.{ DiagrammedAssertions, FunSuite }

class TestWithImages extends FunSuite with DiagrammedAssertions {

  /** Place Red points everywhere on 'a' letter,
   *  then place Black points everywhere on 'A' letter,
   *  then place Red points everywhere on 'b' letter,
   *  then place Black points everywhere on 'B' letter,
   *  etc...
   */
  def constructMoveList(image: Vector[String]) = {
    (for {
      (line, y) <- image.zipWithIndex
      (char, x) <- line.zipWithIndex
      if char.toLower != char.toUpper
    } yield {
      Tuple2(char, Pos(x + 1, y + 1))
    }).sortBy {
      case (char, pos) => char.toLower -> char.isLower
    }.map {
      case (char, pos) => PosPlayer(pos, if (char.isLower) Player.Red else Player.Black)
    }
  }

  def constructField(image: String) = {
    val lines = image.stripMargin.lines.toVector.map(_.trim).filter(_.nonEmpty)
    constructMoveList(lines).foldLeft {
      Field(lines.head.length + 1, lines.size + 1)
    } {
      case (field, newPos) => field.putPoint(newPos.pos, newPos.player)
    }
  }

  test("simple surround") {
    val field = constructField(
      """
      .a.
      cBa
      .a.
      """)
    assert(field.scoreBlack == 0)
    assert(field.scoreRed == 1)
  }

  test("surround empty territory") {
    val field = constructField(
      """
      .a.
      a.a
      .a.
      """)
    assert(field.scoreBlack == 0)
    assert(field.scoreRed == 0)
    assert(field.isPuttingAllowed(Pos(2, 2)))
    assert(!field.isPuttingAllowed(Pos(1, 2)))
    assert(!field.isPuttingAllowed(Pos(2, 1)))
    assert(!field.isPuttingAllowed(Pos(2, 3)))
  }

  test("double surround") {
    val field = constructField(
      """
      .b.b..
      bAzAb.
      .b.b..
      """)
    assert(field.scoreBlack == 0)
    assert(field.scoreRed == 2)
    // commented out for investigation to "kurnevsky".
    // assert(field.surroundChains.size == 2)
  }

}
