package net.pointsgame.domain.api

import argonaut._
import Argonaut._

sealed trait Answer {
  def qId: Option[String]
}

case class ErrorAnswer(qId: Option[String], msg: String) extends Answer

case class RegisterAnswer(qId: Option[String], id: Int) extends Answer

object Answer {
  implicit val errorAnswerEncodeJson = EncodeJson { (answer: ErrorAnswer) =>
    ("qId" :=? answer.qId) ->?: ("msg" := answer.msg) ->: jEmptyObject
  }
  implicit val registerAnswerEncodeJson = EncodeJson { (answer: RegisterAnswer) =>
    ("qId" :=? answer.qId) ->?: ("id" := answer.id) ->: jEmptyObject
  }
  implicit val answerEncodeJson = EncodeJson { (answer: Answer) =>
    val json = answer match {
      case a: ErrorAnswer    => errorAnswerEncodeJson.encode(a)
      case a: RegisterAnswer => registerAnswerEncodeJson.encode(a)
    }
    answer match {
      case _: ErrorAnswer => ("type" := "error") ->: json
      case _              => ("type" := "answer") ->: json
    }
  }
}
