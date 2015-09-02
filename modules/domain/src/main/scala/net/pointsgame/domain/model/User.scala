package net.pointsgame.domain.model

import com.github.nscala_time.time.Imports._
import net.pointsgame.domain.Constants

case class User(id: Option[Int], name: String, password: String, registrationDate: DateTime) extends Entity {
  assert(name.nonEmpty)
  assert(name.length <= Constants.maxNameLength)
}
