package net.pointsgame.paper_engine

import org.scalatest.FunSuite

trait Images { self: FunSuite =>

  /** @param size = field size */
  def rotations(size: Int): List[Pos => Pos] = List[Pos => Pos](
    { case Pos(x, y) => Pos(x, y) },
    { case Pos(x, y) => Pos(size - x, y) },
    { case Pos(x, y) => Pos(x, size - y) },
    { case Pos(x, y) => Pos(size - x, size - y) },
    { case Pos(x, y) => Pos(y, x) },
    { case Pos(x, y) => Pos(size - y, x) },
    { case Pos(x, y) => Pos(y, size - x) },
    { case Pos(x, y) => Pos(size - y, size - x) }
  )

  /** Every letter means a dot that should be placed on the field.
   *  Lower-cases are always Red, upper-cases are always Black.
   *  Order by which appropriate points are placed:
   *  all 'a' points (Red), all 'A' points (Black),
   *  all 'b' points (Red), all 'B' points (Black), etc...
   */
  def constructMoveList(image: Vector[String]) =
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

  def constructFields(image: String) = {
    val lines = image.stripMargin.lines.toVector.map(_.trim).filter(_.nonEmpty)
    require(lines.groupBy(_.length).size == 1, "lines must have equal length")

    constructMoveList(lines).foldLeft {
      List(Field(lines.head.length + 2, lines.size + 2))
    } {
      case (fields @ (h :: _), newPos) => h.putPoint(newPos.pos, newPos.player) :: fields
      case _                           => throw new IllegalStateException("empty list with fields")
    }
  }

  def surroundings(fields: List[Field]) =
    fields.flatMap(_.lastSurroundChain)

  def imgTest(name: String)(image: String)(f: List[Field] => Unit): Unit =
    test(name) {
      val fields = constructFields(image)
      f(fields)
    }

  def lastFieldImgTest(name: String)(image: String)(f: (Field, List[ColoredChain]) => Unit): Unit =
    imgTest(name)(image) { fields =>
      f(fields.head, surroundings(fields))
    }
}
