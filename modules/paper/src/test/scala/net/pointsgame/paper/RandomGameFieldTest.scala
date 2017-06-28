package net.pointsgame.paper

import org.scalacheck.Prop._
import org.scalacheck._
import org.scalatest.prop.Checkers
import org.scalatest.{ DiagrammedAssertions, FunSuite }
import scala.util.{ Random, Try }

/** Plays random games and checks basic consistency */
class RandomGameFieldTest extends FunSuite with DiagrammedAssertions with Checkers {
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
    finalFieldTry.map { finalField =>
      all(
        (finalField.scoreRed >= 0) :| "red score should be non-negative",
        (finalField.scoreBlack >= 0) :| "black score should be non-negative",
        ((field.scoreRed - field.scoreBlack).abs < width * height / 2) :| "score difference should be less than number of player moves",
        (field.scoreRed + field.scoreBlack <= (width - 2) * (height - 2)) :| "full score should be less than field size"
      )
    }.getOrElse(falsified :| "should be no exceptions")
  }))

}
