package net.pointsgame.paper

object Images {
  /**
   * Every letter means a dot that should be placed on the field.
   *  Lower-cases are always Red, upper-cases are always Black.
   *  Order by which appropriate points are placed:
   *  all 'a' points (Red), all 'A' points (Black),
   *  all 'b' points (Red), all 'B' points (Black), etc...
   */
  def constructMoveList(image: String): (Int, Int, List[ColoredPos]) = {
    val lines = image.stripMargin.lines.toList.map(_.trim).filter(_.nonEmpty)
    require(lines.groupBy(_.length).size == 1, "lines must have equal length")
    val width = lines.head.length
    val height = lines.size
    val moves = (for {
      (line, y) <- lines.zipWithIndex
      (char, x) <- line.zipWithIndex
      if char.toLower != char.toUpper
    } yield {
      char -> Pos(x, y)
    }).sortBy {
      case (char, _) => char.toLower -> char.isLower
    }.map {
      case (char, pos) => ColoredPos(pos, Player(char.isLower))
    }
    (width, height, moves)
  }

  def rotations(size: Int): List[Pos => Pos] = List[Pos => Pos](
    { case Pos(x, y) => Pos(x, y) },
    { case Pos(x, y) => Pos(size - 1 - x, y) },
    { case Pos(x, y) => Pos(x, size - 1 - y) },
    { case Pos(x, y) => Pos(size - 1 - x, size - 1 - y) },
    { case Pos(x, y) => Pos(y, x) },
    { case Pos(x, y) => Pos(size - 1 - y, x) },
    { case Pos(x, y) => Pos(y, size - 1 - x) },
    { case Pos(x, y) => Pos(size - 1 - y, size - 1 - x) }
  )

  def constructFieldsFromMoves(width: Int, height: Int, moves: List[ColoredPos]): List[Field] =
    moves.foldLeft {
      List(Field(width, height))
    } {
      (fields, cp) => fields.head.putPoint(cp.pos, cp.player) :: fields
    }

  def constructFieldsFromMovesWithRotations(width: Int, height: Int, moves: List[ColoredPos]): List[(List[Field], Pos => Pos)] = {
    val fieldSize = math.max(width, height)
    rotations(fieldSize).map { rotate =>
      val rotatedMoves = moves.map(cp => cp.copy(pos = rotate(cp.pos)))
      constructFieldsFromMoves(fieldSize, fieldSize, rotatedMoves) -> rotate
    }
  }

  def surroundings(fields: List[Field]) =
    fields.flatMap(_.lastSurroundChain)

  def constructFields(image: String): List[Field] =
    (constructFieldsFromMoves _).tupled(constructMoveList(image))

  def constructLastField(image: String): (Field, List[ColoredChain]) = {
    val fields = constructFields(image)
    (fields.head, surroundings(fields))
  }

  def constructFieldsWithRotations(image: String): List[(List[Field], Pos => Pos)] =
    (constructFieldsFromMovesWithRotations _).tupled(constructMoveList(image))

  def constructLastFieldWithRotations(image: String): List[(Field, List[ColoredChain], Pos => Pos)] =
    constructFieldsWithRotations(image).map {
      case (fields, rotate) =>
        (fields.head, surroundings(fields), rotate)
    }
}
