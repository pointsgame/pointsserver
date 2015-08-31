package net.pointsgame.domain.model

import net.pointsgame.domain.Constants

case class User(id: Option[Int], name: String) extends Entity {
  assert(name.nonEmpty)
  assert(name.length <= Constants.maxNameLength)
}
