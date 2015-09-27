package net.pointsgame.domain.model

trait Entity {
  def id: Option[Long]
  def isNew: Boolean =
    id.isEmpty
}
