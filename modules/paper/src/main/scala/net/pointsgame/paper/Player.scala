package net.pointsgame.paper

sealed trait Player {
  def opponent: Player
}

object Player {
  case object Red extends Player {
    override def opponent: Player =
      Black
  }
  case object Black extends Player {
    override def opponent: Player =
      Red
  }
  def apply(boolean: Boolean): Player = {
    if (boolean)
      Red
    else
      Black
  }
}
