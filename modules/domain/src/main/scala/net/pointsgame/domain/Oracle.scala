package net.pointsgame.domain

import scalaz._
import Scalaz._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import net.pointsgame.domain.services.AccountService
import net.pointsgame.domain.api._

final class Oracle(accountService: AccountService) {
  private var userId = none[Int]
  def answer(question: TokenizedQuestion[Question]): Future[Answer] = {
    ???
  }
  def answer(question: Question): Future[Answer] = {
    question match {
      case RegisterQuestion(qId, name, password) =>
        accountService.register(name, password).map(RegisterAnswer(qId, _))
    }
  } recover {
    case e: DomainException =>
      ErrorAnswer(question.qId, e.getMessage)
  }
}
