package net.pointsgame.domain.api

import argonaut._
import Argonaut._

sealed trait Answer {
  def qId: Option[String]
}

final case class ErrorAnswer(qId: Option[String], msg: String) extends Answer

final case class RegisterAnswer(qId: Option[String], id: Int, token: String) extends Answer

final case class LoginAnswer(qId: Option[String], id: Int, token: String) extends Answer

final case class SendRoomMessageAnswer(qId: Option[String], id: Int) extends Answer

final case class SubscribeToRoomAnswer(qId: Option[String]) extends Answer

object Answer {
  implicit val errorAnswerEncodeJson = EncodeJson { (answer: ErrorAnswer) =>
    ("qId" :=? answer.qId) ->?: ("msg" := answer.msg) ->: jEmptyObject
  }
  implicit val registerAnswerEncodeJson = EncodeJson { (answer: RegisterAnswer) =>
    ("qId" :=? answer.qId) ->?: ("id" := answer.id) ->: ("token" := answer.token) ->: jEmptyObject
  }
  implicit val loginAnswerEncodeJson = EncodeJson { (answer: LoginAnswer) =>
    ("qId" :=? answer.qId) ->?: ("id" := answer.id) ->: ("token" := answer.token) ->: jEmptyObject
  }
  implicit val sendRoomMessageAnswerEncodeJson = EncodeJson { (answer: SendRoomMessageAnswer) =>
    ("qId" :=? answer.qId) ->?: ("id" := answer.id) ->: jEmptyObject
  }
  implicit val subscribeToRoomAnswerEncodeJson = EncodeJson { (answer: SubscribeToRoomAnswer) =>
    ("qId" :=? answer.qId) ->?: jEmptyObject
  }
  implicit val answerEncodeJson = EncodeJson { (answer: Answer) =>
    val json = answer match {
      case a: ErrorAnswer           => errorAnswerEncodeJson.encode(a)
      case a: RegisterAnswer        => ("question" := "register") ->: registerAnswerEncodeJson.encode(a)
      case a: LoginAnswer           => ("question" := "login") ->: loginAnswerEncodeJson.encode(a)
      case a: SendRoomMessageAnswer => ("question" := "sendRoomMessage") ->: sendRoomMessageAnswerEncodeJson.encode(a)
      case a: SubscribeToRoomAnswer => ("question" := "subscribeToRoom") ->: subscribeToRoomAnswerEncodeJson.encode(a)
    }
    answer match {
      case _: ErrorAnswer => ("type" := "error") ->: json
      case _              => ("type" := "answer") ->: json
    }
  }
}
