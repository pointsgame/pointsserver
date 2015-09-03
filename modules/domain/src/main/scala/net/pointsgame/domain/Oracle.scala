package net.pointsgame.domain

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import net.pointsgame.domain.api._
import net.pointsgame.domain.helpers.Tokenizer

final class Oracle(services: Services, var delivery: Delivery => Unit = _ => ()) {
  lazy val connectionId = Tokenizer.generate(Constants.connectionIdLength)
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
