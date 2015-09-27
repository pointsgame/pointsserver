package net.pointsgame.domain.model

import com.github.nscala_time.time.Imports._

case class RoomMessage(id: Option[Long], body: String, roomId: Long, senderId: Long, sendingDate: DateTime) extends Entity {
  assert(body.nonEmpty)
}
