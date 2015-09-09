package net.pointsgame.domain.services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.nscala_time.time.Imports._
import net.pointsgame.domain.repositories.{ RoomRepository, RoomMessageRepository }
import net.pointsgame.domain.DomainException
import net.pointsgame.domain.model.RoomMessage
import net.pointsgame.domain.helpers.Validator

final class RoomMessageService(roomMessageRepository: RoomMessageRepository, roomRepository: RoomRepository, accountService: AccountService) {
  def send(userId: Int, roomId: Int, body: String): Future[Int] = Validator.checkMessageBody(body) {
    for {
      exists <- roomRepository.exists(roomId)
      message = RoomMessage(None, body, roomId, userId, DateTime.now())
      messageId <- if (exists) {
        roomMessageRepository.insert(message)
      } else {
        Future.failed(new DomainException("Room with such name doesn't exist."))
      }
    } yield messageId
  }
}
