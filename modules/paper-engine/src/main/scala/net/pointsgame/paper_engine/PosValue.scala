package net.pointsgame.paper_engine

sealed trait PosValue {
  def isFree: Boolean
  def isEmptyBase(player: Player): Boolean
  def isPlayer(player: Player): Boolean
}

case object EmptyPosValue extends PosValue {
  override def isFree: Boolean =
    true
  override def isEmptyBase(player: Player): Boolean =
    false
  override def isPlayer(player: Player): Boolean =
    false
}

final case class PlayerPosValue(player: Player) extends PosValue {
  override def isFree: Boolean =
    false
  override def isEmptyBase(player: Player): Boolean =
    false
  override def isPlayer(player: Player): Boolean =
    this.player == player
}

final case class EmptyBasePosValue(player: Player) extends PosValue {
  override def isFree: Boolean =
    true
  override def isEmptyBase(player: Player): Boolean =
    this.player == player
  override def isPlayer(player: Player): Boolean =
    false
}
