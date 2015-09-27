package net.pointsgame.domain

import scalaz.concurrent.Task
import net.pointsgame.domain.api._

final case class Oracle(services: Services, managers: Managers) {
  def answer(httpQuestion: HttpQuestion): Task[HttpAnswer] = (httpQuestion match {
    case SignInHttpQuestion(name, password) =>
      services.accountService.register(name, password) map SignInHttpAnswer.tupled
    case LogInHttpQuestion(name, password) =>
      services.accountService.login(name, password) map LogInHttpAnswer.tupled
    case SendRoomMessageHttpQuestion(token, roomId, body) => services.accountService.withUser(token) { user =>
      managers.connectionManager.wasSeenUserId(user.id.get)
      for (roomMessage <- services.roomMessageService.send(user.id.get, roomId, body)) yield {
        managers.roomMessageManager.send(roomMessage)
        SendRoomMessageHttpAnswer(roomMessage.id.get)
      }
    }
  }).handle {
    case e: DomainException =>
      ErrorHttpAnswer(0, e.getMessage)
  }
  def answer(connectionId: Long, wsQuestion: WsQuestion): Task[WsAnswer] = (wsQuestion match {
    case IntroducingWsQuestion(qId, token) => services.accountService.withUser(token) { user =>
      managers.connectionManager.updateUserId(connectionId, user.id.get)
      Task.now(IntroducingWsAnswer(qId))
    }
    case SubscribeToRoomWsQuestion(qId, roomId) =>
      managers.roomMessageManager.subscribe(roomId, connectionId)
      Task.now(SubscribeToRoomWsAnswer(qId, roomId))
  }).handle {
    case e: DomainException =>
      ErrorWsAnswer(wsQuestion.qId, 0, e.getMessage)
  }
  def newWS(callback: WsDelivery => Unit): Long =
    managers.connectionManager.newConnection(callback)
  def close(connectionId: Long): Unit =
    managers.connectionManager.remove(connectionId)
}
