package net.pointsgame.domain.model

case class Room(id: Option[Long], name: String) extends Entity {
  assert(name.nonEmpty)
}
