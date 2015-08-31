package net.pointsgame.server

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import net.pointsgame.server.api._
import net.pointsgame.domain.services.AccountService
import net.pointsgame.domain.DomainException

final case class Oracle(accountService: AccountService) {
  def answer(question: Question): Future[Answer] = {
    question match {
      case RegisterQuestion(name, password) =>
        accountService.register(name).map(RegisterAnswer)
    }
  } recover {
    case e: DomainException =>
      ErrorAnswer(e.getMessage)
  }
}
