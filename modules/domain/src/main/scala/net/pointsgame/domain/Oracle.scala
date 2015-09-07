package net.pointsgame.domain

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.concurrent
import net.pointsgame.domain.api._

final class Oracle(services: Services) {
  private val connections = concurrent.TrieMap.empty[String, Delivery => Unit]
  def connect(connectionId: String, callback: Delivery => Unit): Unit = {
    connections += connectionId -> callback
    callback(ConnectedDelivery(connectionId))
  }
  def disconnect(connectionId: String): Unit =
    connections -= connectionId
  private def recover(qId: Option[String])(answer: Future[Answer]): Future[Answer] =
    answer.recover {
      case e: DomainException =>
        ErrorAnswer(qId, e.getMessage)
    }
  def register(qId: Option[String], name: String, password: String): Future[Answer] = recover(qId) {
    services.accountService.register(name, password) map { RegisterAnswer(qId, _: Int, _: String) }.tupled
  }
  def login(qId: Option[String], name: String, password: String): Future[Answer] = recover(qId) {
    services.accountService.login(name, password) map { LoginAnswer(qId, _: Int, _: String) }.tupled
  }
  def sendRoomMessage(qId: Option[String], token: String, roomId: Int, body: String): Future[Answer] = recover(qId) {
    services.roomMessageService.send(token, roomId, body) map { SendRoomMessageAnswer(qId, _) }
  }
  def answer(question: Question): Future[Answer] = question match {
    case RegisterQuestion(qId, name, password) =>
      register(qId, name, password)
    case LoginQuestion(qId, name, password) =>
      login(qId, name, password)
    case SendRoomMessageQuestion(qId, token, roomId, body) =>
      sendRoomMessage(qId, token, roomId, body)
  }
}
