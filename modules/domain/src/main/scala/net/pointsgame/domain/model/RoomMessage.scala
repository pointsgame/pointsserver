package net.pointsgame.domain.model

import com.github.nscala_time.time.Imports._

case class RoomMessage(id: Option[Int], body: String, roomId: Int, senderId: Int, sendingDate: DateTime) extends Entity {
  assert(body.nonEmpty)
}
