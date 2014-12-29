package net.pointsgame.paper_engine

import scala.util.{ Random, Try }
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.scalacheck._
import org.scalacheck.Prop._

class FieldTest extends FunSuite with Checkers {
  test("simple surround") {
    val field = Field(10, 10)
      .putPoint(Pos(5, 5), Player.Black)
      .putPoint(Pos(4, 5), Player.Red)
      .putPoint(Pos(5, 4), Player.Red)
      .putPoint(Pos(6, 5), Player.Red)
      .putPoint(Pos(5, 6), Player.Red)
    assert(field.scoreBlack == 0)
    assert(field.scoreRed == 1)
    assert(field.isPlayersPoint(Pos(5, 5), Player.Red))
  }

  val minSize = 3
  val maxSize = 50
  val lengthGen = Gen.choose(minSize, maxSize)
  val seedGen = Arbitrary.arbitrary[Int]
  test("random game")(check(Prop.forAll(lengthGen, lengthGen, seedGen) { (width: Int, height: Int, seed: Int) =>
    val random = new Random(seed)
    val moves = random.shuffle((0 until width * height).toVector).map(idx => Pos(idx % width, idx / width))
    val field = Field(width, height)
    val finalFieldTry = Try {
      moves.foldLeft(field)((acc, pos) => if (acc.isPuttingAllowed(pos)) acc.putPoint(pos) else acc)
    }
    val propsTry = finalFieldTry.map { finalField =>
      all(
        (finalField.scoreRed >= 0) :| "Red score should be non-negative.",
        (finalField.scoreBlack >= 0) :| "Black score should be non-negative.",
        ((field.scoreRed - field.scoreBlack).abs < width * height / 2) :| "Score difference should be less than number of player moves",
        (field.scoreRed + field.scoreBlack <= (width - 2) * (height - 2)) :| "Full score should be less than field size."
      )
    }
    all(
      finalFieldTry.isSuccess :| "Should not be exceptions.",
      propsTry.get
    )
  }))
}
