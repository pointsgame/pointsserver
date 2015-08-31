package net.pointsgame.domain.model

trait Entity {
  def id: Option[Int]
  def isNew: Boolean =
    id.isEmpty
}
