package net.pointsgame.domain

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import net.pointsgame.domain.services.AccountService
import net.pointsgame.domain.api._

final class Oracle(accountService: AccountService) {
  def answer(question: TokenizedQuestion[Question]): Future[Answer] = {
    ???
  }
  def answer(question: Question): Future[Answer] = {
    question match {
      case RegisterQuestion(qId, name, password) =>
        accountService.register(name).map(RegisterAnswer(qId, _))
    }
  } recover {
    case e: DomainException =>
      ErrorAnswer(question.qId, e.getMessage)
  }
}
