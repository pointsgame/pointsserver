package net.pointsgame.paper_engine

import org.scalatest.{ DiagrammedAssertions, FunSuite }

class TestWithImages extends FunSuite with DiagrammedAssertions {

  test("simple surround") {
    val (field, surroundings) = constructField(
      """
      .a.
      cBa
      .a.
      """
    )
    assert(field.scoreRed == 1)
    assert(field.scoreBlack == 0)
    assert(surroundings.size == 1)
  }

  test("surround empty territory") {
    val (field, surroundings) = constructField(
      """
      .a.
      a.a
      .a.
      """
    )
    assert(field.scoreRed == 0)
    assert(field.scoreBlack == 0)
    assert(surroundings.size == 0)
    assert(field.isPuttingAllowed(Pos(2, 2)))
    assert(!field.isPuttingAllowed(Pos(1, 2)))
    assert(!field.isPuttingAllowed(Pos(2, 1)))
    assert(!field.isPuttingAllowed(Pos(2, 3)))
  }

  test("apply 'control' surrounding in same turn") {
    val (field, surroundings) = constructField(
      """
      .a.
      aBa
      .a.
      """
    )
    assert(field.scoreRed == 1)
    assert(field.scoreBlack == 0)
    assert(surroundings.size == 1)
  }

  test("double surround") {
    val (field, surroundings) = constructField(
      """
      .b.b..
      bAzAb.
      .b.b..
      """
    )
    assert(field.scoreRed == 2)
    assert(field.scoreBlack == 0)

    // These assertions rely on `Field` conventions.
    // We assume there can be exactly one surrounding per turn
    // (but the surrounding may seem like two separate surroundings on GUI).
    assert(field.lastSurroundChain.map(_.chain.size) == Some(8))
    assert(surroundings.size == 1)
  }

  test("double surround with empty part") {
    val (field, surroundings) = constructField(
      """
      .b.b..
      b.zAb.
      .b.b..
      """
    )
    assert(field.scoreRed == 1)
    assert(field.scoreBlack == 0)
    assert(surroundings.size == 1)
    assert(field.isPuttingAllowed(Pos(2, 2)))
    assert(!field.isPuttingAllowed(Pos(4, 2)))
  }

  test("should not leave empty inside") {
    val (field, surroundings) = constructField(
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
    assert(surroundings.size == 1)

    assert(!field.isPuttingAllowed(Pos(3, 4)))

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
      char -> Pos(x + 1, y + 1)
    }).sortBy {
      case (char, _) => char.toLower -> char.isLower
    }.map {
      case (char, pos) => ColoredPos(pos, Player(char.isLower))
    }
  }

  def constructField(image: String) = {
    val lines = image.stripMargin.lines.toVector.map(_.trim).filter(_.nonEmpty)
    require(lines.groupBy(_.length).size == 1, "lines must have equal length")

    constructMoveList(lines).foldLeft {
      Field(lines.head.length + 1, lines.size + 1) -> Vector.empty[ColoredChain]
    } {
      case ((field, surroundings), newPos) =>
        val newField = field.putPoint(newPos.pos, newPos.player)
        newField -> (surroundings ++ newField.lastSurroundChain)
    }
  }

}
