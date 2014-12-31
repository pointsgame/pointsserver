package net.pointsgame.paper_engine

import org.scalatest.{ DiagrammedAssertions, FunSuite }

class TestWithImages extends FunSuite with DiagrammedAssertions {

  test("simple surround") {
    val field = constructField(
      """
      .a.
      cBa
      .a.
      """)
    assert(field.scoreRed == 1)
    assert(field.scoreBlack == 0)
  }

  test("surround empty territory") {
    val field = constructField(
      """
      .a.
      a.a
      .a.
      """)
    assert(field.scoreRed == 0)
    assert(field.scoreBlack == 0)
    assert(field.isPuttingAllowed(Pos(2, 2)))
    assert(!field.isPuttingAllowed(Pos(1, 2)))
    assert(!field.isPuttingAllowed(Pos(2, 1)))
    assert(!field.isPuttingAllowed(Pos(2, 3)))
  }

  test("apply 'control' surrounding in same turn") {
    val field = constructField(
      """
      .a.
      aBa
      .a.
      """)
    // investigation by "kurnevsky" required
    assert(field.scoreRed == 1)
    assert(field.scoreBlack == 0)
  }

  test("double surround") {
    val field = constructField(
      """
      .b.b..
      bAzAb.
      .b.b..
      """)
    assert(field.scoreRed == 2)
    assert(field.scoreBlack == 0)

    // investigation by "kurnevsky" required
    assert(field.surroundChains.size == 2)
  }

  test("double surround with empty part") {
    val field = constructField(
      """
      .b.b..
      b.zAb.
      .b.b..
      """)
    assert(field.scoreRed == 1)
    assert(field.scoreBlack == 0)
    assert(field.isPuttingAllowed(Pos(2, 2)))
    assert(!field.isPuttingAllowed(Pos(4, 2)))
  }

  test("should leave empty inside") {
    val field = constructField(
      """
        .aaaa..
        a....a.
        a.b...a
        .z.bC.a
        a.b...a
        a....a.
        .aaaa..
      """
    )
    assert(field.scoreRed == 1)
    assert(field.scoreBlack == 0)

    assert(field.isPuttingAllowed(Pos(3, 4)))

    assert(!field.isPuttingAllowed(Pos(3, 5)))
    assert(!field.isPuttingAllowed(Pos(3, 3)))
    assert(!field.isPuttingAllowed(Pos(2, 4)))
    assert(!field.isPuttingAllowed(Pos(4, 4)))

    assert(!field.isPuttingAllowed(Pos(2, 2)))
  }

  /** Every letter means a dot that should be placed on the field.
   *  Lower-cases are always Red, upper-cases are always Black.
   *  Order by which appropriate points are placed:
   *  all 'a' points (Red), all 'A' points (Black),
   *  all 'b' points (Red), all 'B' points (Black), etc...
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
    assert(lines.groupBy(_.length).size == 1, "lines must have equal length")

    constructMoveList(lines).foldLeft {
      Field(lines.head.length + 1, lines.size + 1)
    } {
      case (field, newPos) => field.putPoint(newPos.pos, newPos.player)
    }
  }

}
