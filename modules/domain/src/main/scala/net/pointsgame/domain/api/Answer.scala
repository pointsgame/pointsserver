package net.pointsgame.domain.api

import argonaut._
import Argonaut._

sealed trait Answer {
  def qId: Option[String]
}

case class ErrorAnswer(qId: Option[String], msg: String) extends Answer

case class RegisterAnswer(qId: Option[String], id: Int) extends Answer

object Answer {
  implicit val answerEncodeJson = EncodeJson { (answer: Answer) =>
    answer match {
      case ErrorAnswer(_, msg)   => ("type" := "error") ->: ("msg" := msg) ->: jEmptyObject
      case RegisterAnswer(_, id) => ("type" := "answer") ->: ("question" := "register") ->: ("id" := id) ->: jEmptyObject
    }
  }
}
