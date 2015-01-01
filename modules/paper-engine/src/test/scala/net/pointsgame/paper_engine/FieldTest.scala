package net.pointsgame.paper_engine

import scala.util.{ Random, Try }
import org.scalatest.{ DiagrammedAssertions, FunSuite }
import org.scalatest.prop.Checkers
import org.scalacheck._
import org.scalacheck.Prop._

class FieldTest extends FunSuite with DiagrammedAssertions with Checkers with Images {
  lastFieldImgTest("simple surround")(
    """
    .a.
    cBa
    .a.
    """
  ) { (field, surroundings) =>
      assert(field.scoreRed == 1)
      assert(field.scoreBlack == 0)
      assert(surroundings.size == 1)
    }

  lastFieldImgTest("surround empty territory")(
    """
    .a.
    a.a
    .a.
    """
  ) { (field, surroundings) =>
      assert(field.scoreRed == 0)
      assert(field.scoreBlack == 0)
      assert(surroundings.size == 0)
      assert(field.isPuttingAllowed(Pos(2, 2)))
      assert(!field.isPuttingAllowed(Pos(1, 2)))
      assert(!field.isPuttingAllowed(Pos(2, 1)))
      assert(!field.isPuttingAllowed(Pos(2, 3)))
    }

  lastFieldImgTest("move priority")(
    """
    .aB.
    aCaB
    .aB.
    """
  ) { (field, surroundings) =>
      assert(field.scoreRed == 0)
      assert(field.scoreBlack == 1)
      assert(surroundings.size == 1)
    }

  lastFieldImgTest("move priority, big")(
    """
    .B..
    BaB.
    aCaB
    .aB.
    """
  ) { (field, surroundings) =>
      assert(field.scoreRed == 0)
      assert(field.scoreBlack == 2)
      assert(surroundings.size == 1)
    }

  lastFieldImgTest("onion surroundings")(
    """
    ...c...
    ..cBc..
    .cBaBc.
    ..cBc..
    ...c...
    """
  ) { (field, surroundings) =>
      assert(field.scoreRed == 4)
      assert(field.scoreBlack == 0)
      assert(surroundings.size == 2)
    }

  lastFieldImgTest("apply 'control' surrounding in same turn")(
    """
    .a.
    aBa
    .a.
    """
  ) { (field, surroundings) =>
      assert(field.scoreRed == 1)
      assert(field.scoreBlack == 0)
      assert(surroundings.size == 1)
    }

  lastFieldImgTest("double surround")(
    """
    .b.b..
    bAzAb.
    .b.b..
    """
  ) { (field, surroundings) =>
      assert(field.scoreRed == 2)
      assert(field.scoreBlack == 0)

      // These assertions rely on `Field` conventions.
      // We assume there can be exactly one surrounding per turn
      // (but the surrounding may seem like two separate surroundings on GUI).
      assert(field.lastSurroundChain.map(_.chain.size) == Some(8))
      assert(surroundings.size == 1)
    }

  lastFieldImgTest("double surround with empty part")(
    """
    .b.b..
    b.zAb.
    .b.b..
    """
  ) { (field, surroundings) =>
      assert(field.scoreRed == 1)
      assert(field.scoreBlack == 0)
      assert(surroundings.size == 1)
      assert(field.isPuttingAllowed(Pos(2, 2)))
      assert(!field.isPuttingAllowed(Pos(4, 2)))
    }

  lastFieldImgTest("should not leave empty inside")(
    """
    .aaaa..
    a....a.
    a.b...a
    .z.bC.a
    a.b...a
    a....a.
    .aaaa..
    """
  ) { (field, surroundings) =>
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

  lastFieldImgTest("a hole inside a surrounding")(
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
  ) { (field, surroundings) =>
      assert(field.scoreRed == 1)
      assert(field.scoreBlack == 0)
      assert(surroundings.size == 1)
      assert(!field.isPuttingAllowed(Pos(5, 5)))
      assert(!field.isPuttingAllowed(Pos(5, 2)))
    }

  lastFieldImgTest("a hole inside a surrounding, after 'control' surrounding")(
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
  ) { (field, surroundings) =>
      assert(field.scoreRed == 1)
      assert(field.scoreBlack == 0)
      assert(surroundings.size == 1)
      assert(!field.isPuttingAllowed(Pos(5, 5)))
      assert(!field.isPuttingAllowed(Pos(5, 2)))
    }

  lastFieldImgTest("surrounding does not expand")(
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
  ) { (field, surroundings) =>
      assert(field.scoreRed == 1)
      assert(field.scoreBlack == 0)
      assert(surroundings.size == 1)

      assert(field.lastSurroundChain.map(_.chain.size) == Some(4))

      assert(field.isPuttingAllowed(Pos(7, 4)))
      assert(field.isPuttingAllowed(Pos(5, 4)))
      assert(field.isPuttingAllowed(Pos(5, 6)))
      assert(field.isPuttingAllowed(Pos(7, 6)))

      assert(!field.isPuttingAllowed(Pos(6, 5)))
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
