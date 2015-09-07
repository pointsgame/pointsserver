package net.pointsgame.domain.services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.nscala_time.time.Imports._
import net.pointsgame.domain.repositories.{ RoomRepository, RoomMessageRepository }
import net.pointsgame.domain.DomainException
import net.pointsgame.domain.model.RoomMessage
import net.pointsgame.domain.helpers.Validator

final class RoomMessageService(roomMessageRepository: RoomMessageRepository, roomRepository: RoomRepository, accountService: AccountService) {
  def send(tokenString: String, roomId: Int, body: String): Future[Int] = Validator.checkMessageBody(body) {
    accountService.withUser(tokenString) { user =>
      for {
        exists <- roomRepository.exists(roomId)
        messageId <- if (exists) {
          roomMessageRepository.insert(RoomMessage(None, body, roomId, user.id.get, DateTime.now()))
        } else {
          Future.failed(new DomainException("Room with such name doesn't exist."))
        }
      } yield messageId
    }
  }
}
