package net.pointsgame.domain.services

import scala.concurrent.Future
import net.pointsgame.domain.repositories.RoomMessageRepository

final class RoomMessageService(roomMessageRepository: RoomMessageRepository) {
  def send(token: String, roomId: Int, body: String): Future[Int] =
    ???
}
