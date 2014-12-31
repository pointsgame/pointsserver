package net.pointsgame.paper_engine

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
