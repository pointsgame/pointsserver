package net.pointsgame.paper_engine

import scala.util.{ Random, Try }
import org.scalatest.{ DiagrammedAssertions, FunSuite }
import org.scalatest.prop.Checkers
import org.scalacheck._
import org.scalacheck.Prop._

class FieldTest extends FunSuite with DiagrammedAssertions with Checkers {
  import Images._

  test("simple surround") {
    constructLastFieldWithRotations(
      """
      .a.
      cBa
      .a.
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 1)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 1)
      }
  }

  test("surround empty territory") {
    constructLastFieldWithRotations(
      """
      .a.
      a.a
      .a.
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 0)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 0)
          assert(field.isPuttingAllowed(rotate(Pos(1, 1))))
          assert(!field.isPuttingAllowed(rotate(Pos(0, 1))))
          assert(!field.isPuttingAllowed(rotate(Pos(1, 0))))
          assert(!field.isPuttingAllowed(rotate(Pos(1, 2))))
      }
  }

  test("move priority") {
    constructLastFieldWithRotations(
      """
      .aB.
      aCaB
      .aB.
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 0)
          assert(field.scoreBlack == 1)
          assert(surroundings.size == 1)
      }
  }

  test("move priority, big") {
    constructLastFieldWithRotations(
      """
      .B..
      BaB.
      aCaB
      .aB.
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 0)
          assert(field.scoreBlack == 2)
          assert(surroundings.size == 1)
      }
  }

  test("onion surroundings") {
    constructLastFieldWithRotations(
      """
      ...c...
      ..cBc..
      .cBaBc.
      ..cBc..
      ...c...
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 4)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 2)
      }
  }

  test("apply 'control' surrounding in same turn") {
    constructLastFieldWithRotations(
      """
      .a.
      aBa
      .a.
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 1)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 1)
      }
  }

  test("double surround") {
    constructLastFieldWithRotations(
      """
      .b.b..
      bAzAb.
      .b.b..
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 2)
          assert(field.scoreBlack == 0)

          // These assertions rely on `Field` conventions.
          // We assume there can be exactly one surrounding per turn
          // (but the surrounding may seem like two separate surroundings on GUI).
          assert(field.lastSurroundChain.map(_.chain.size) == Some(8))
          assert(surroundings.size == 1)
      }
  }

  test("double surround with empty part") {
    constructLastFieldWithRotations(
      """
      .b.b..
      b.zAb.
      .b.b..
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 1)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 1)
          assert(field.isPuttingAllowed(rotate(Pos(1, 1))))
          assert(!field.isPuttingAllowed(rotate(Pos(3, 1))))
      }
  }

  test("should not leave empty inside") {
    constructLastFieldWithRotations(
      """
      .aaaa..
      a....a.
      a.b...a
      .z.bC.a
      a.b...a
      a....a.
      .aaaa..
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 1)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 1)

          assert(!field.isPuttingAllowed(rotate(Pos(2, 3))))

          assert(!field.isPuttingAllowed(rotate(Pos(2, 4))))
          assert(!field.isPuttingAllowed(rotate(Pos(2, 2))))
          assert(!field.isPuttingAllowed(rotate(Pos(1, 3))))
          assert(!field.isPuttingAllowed(rotate(Pos(3, 3))))

          assert(!field.isPuttingAllowed(rotate(Pos(1, 1))))
      }
  }

  test("a hole inside a surrounding") {
    constructLastFieldWithRotations(
      """
      ....c....
      ...c.c...
      ..c...c..
      .c..a..c.
      c..a.a..c
      .c..a..c.
      ..c...c..
      ...cBc...
      ....d....
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 1)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 1)
          assert(!field.isPuttingAllowed(rotate(Pos(4, 4))))
          assert(!field.isPuttingAllowed(rotate(Pos(4, 1))))
      }
  }

  test("a hole inside a surrounding, after 'control' surrounding") {
    constructLastFieldWithRotations(
      """
      ....b....
      ...b.b...
      ..b...b..
      .b..a..b.
      b..a.a..b
      .b..a..b.
      ..b...b..
      ...bCb...
      ....b....
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 1)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 1)
          assert(!field.isPuttingAllowed(rotate(Pos(4, 4))))
          assert(!field.isPuttingAllowed(rotate(Pos(4, 1))))
      }
  }

  test("surrounding does not expand") {
    constructLastFieldWithRotations(
      """
      ....a....
      ...a.a...
      ..a.a.a..
      .a.a.a.a.
      a.a.aBa.a
      .a.a.a.a.
      ..a.a.a..
      ...a.a...
      ....a....
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 1)
          assert(field.scoreBlack == 0)
          assert(surroundings.size == 1)

          assert(field.lastSurroundChain.map(_.chain.size) == Some(4))

          assert(field.isPuttingAllowed(rotate(Pos(6, 3))))
          assert(field.isPuttingAllowed(rotate(Pos(4, 3))))
          assert(field.isPuttingAllowed(rotate(Pos(4, 5))))
          assert(field.isPuttingAllowed(rotate(Pos(6, 5))))

          assert(!field.isPuttingAllowed(rotate(Pos(5, 4))))
      }
  }

  test("2 adjacent surroundings") {
    constructLastFieldWithRotations(
      """
      .a..
      aAa.
      .bAa
      ..a.
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 2)
          assert(field.scoreBlack == 0)

          assert(field.lastSurroundChain.map(_.chain.size) == Some(6))
      }
  }

  test("2 opposite surroundings") {
    constructLastFieldWithRotations(
      """
      .a.a.
      aAbAa
      .a.a.
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 2)
          assert(field.scoreBlack == 0)

          assert(field.lastSurroundChain.map(_.chain.size) == Some(8))
      }
  }

  test("3 surroundings") {
    constructLastFieldWithRotations(
      """
      ..a..
      .aAa.
      ..bAa
      .aAa.
      ..a..
      """
    ).foreach {
        case (field, surroundings, rotate) =>
          assert(field.scoreRed == 3)
          assert(field.scoreBlack == 0)

          assert(field.lastSurroundChain.map(_.chain.size) == Some(8))
      }
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
        (finalField.scoreRed >= 0) :| "red score should be non-negative",
        (finalField.scoreBlack >= 0) :| "black score should be non-negative",
        ((field.scoreRed - field.scoreBlack).abs < width * height / 2) :| "score difference should be less than number of player moves",
        (field.scoreRed + field.scoreBlack <= (width - 2) * (height - 2)) :| "full score should be less than field size"
      )
    }
    all(
      finalFieldTry.isSuccess :| "should be no exceptions",
      propsTry.get
    )
  }))
}
