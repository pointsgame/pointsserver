package net.pointsgame.paper

final case class Pos(x: Int, y: Int) {
  def n: Pos =
    Pos(x, y + 1)
  def s: Pos =
    Pos(x, y - 1)
  def w: Pos =
    Pos(x - 1, y)
  def e: Pos =
    Pos(x + 1, y)
  def nw: Pos =
    Pos(x - 1, y + 1)
  def ne: Pos =
    Pos(x + 1, y + 1)
  def sw: Pos =
    Pos(x - 1, y - 1)
  def se: Pos =
    Pos(x + 1, y - 1)
  def dx(pos: Pos): Int =
    x - pos.x
  def dy(pos: Pos): Int =
    y - pos.y
  def tuple: (Int, Int) =
    (x, y)
}

object Pos extends ((Int, Int) => Pos) {
  implicit val ordering: Ordering[Pos] = new Ordering[Pos] {
    private val tupleOrdering = implicitly[Ordering[(Int, Int)]]
    override def compare(a: Pos, b: Pos): Int =
      tupleOrdering.compare(a.tuple, b.tuple)
  }
}
