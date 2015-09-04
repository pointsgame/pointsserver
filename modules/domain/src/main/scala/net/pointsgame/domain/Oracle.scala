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
  def answer(question: Question): Future[Answer] = {
    question match {
      case RegisterQuestion(qId, token, name, password) =>
        services.accountService.register(name, password).map(RegisterAnswer(qId, _))
      case LoginQuestion(qId, token, name, password) =>
        ???
    }
  } recover {
    case e: DomainException =>
      ErrorAnswer(question.qId, e.getMessage)
  }
}
