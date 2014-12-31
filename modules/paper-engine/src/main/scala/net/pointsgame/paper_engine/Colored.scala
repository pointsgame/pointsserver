package net.pointsgame.paper_engine

trait Colored {
  def player: Player
}

final case class ColoredPos(pos: Pos, player: Player) extends Colored

final case class ColoredChain(chain: List[Pos], player: Player) extends Colored
