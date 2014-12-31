package net.pointsgame.paper_engine

sealed trait Player {
  def next: Player
}

object Player {
  case object Red extends Player {
    override def next: Player =
      Black
  }
  case object Black extends Player {
    override def next: Player =
      Red
  }
  def apply(boolean: Boolean): Player =
    if (boolean)
      Red
    else
      Black
}
