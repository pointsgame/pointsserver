package net.pointsgame.domain.services

import scalaz.concurrent.Task
import com.github.nscala_time.time.Imports._
import net.pointsgame.domain.repositories.{ RoomRepository, RoomMessageRepository }
import net.pointsgame.domain.DomainException
import net.pointsgame.domain.model.RoomMessage
import net.pointsgame.domain.helpers.Validator

final case class RoomMessageService(roomMessageRepository: RoomMessageRepository, roomRepository: RoomRepository, accountService: AccountService) {
  def send(userId: Long, roomId: Long, body: String): Task[RoomMessage] = Validator.checkMessageBody(body) {
    for {
      exists <- roomRepository.exists(roomId)
      message = RoomMessage(None, body, roomId, userId, DateTime.now())
      messageId <- if (exists) {
        roomMessageRepository.insert(message)
      } else {
        Task.fail(new DomainException("Room with such name doesn't exist."))
      }
    } yield message.copy(id = Some(messageId))
  }
}
