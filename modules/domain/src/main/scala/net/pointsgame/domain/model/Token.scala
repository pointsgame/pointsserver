package net.pointsgame.domain.model

import com.github.nscala_time.time.Imports._

case class Token(id: Option[Long], userId: Long, tokenString: String, creationDate: DateTime, lastAccessDate: DateTime, expired: Boolean) extends Entity {
  assert(tokenString.nonEmpty)
}
