package net.pointsgame

import scala.util.Try
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.scalacheck.Prop._

class FieldTest extends FunSuite with Checkers {
  test("Capturing.") {
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
}
