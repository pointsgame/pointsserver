package net.pointsgame.domain.model

case class Room(id: Option[Int], name: String) extends Entity {
  assert(name.nonEmpty)
}
