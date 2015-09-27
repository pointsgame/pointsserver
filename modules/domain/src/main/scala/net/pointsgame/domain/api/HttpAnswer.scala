package net.pointsgame.domain.api

import argonaut._
import Argonaut._

sealed trait HttpAnswer

final case class ErrorHttpAnswer(code: Long, msg: String) extends HttpAnswer

final case class SignInHttpAnswer(userId: Long, token: String) extends HttpAnswer

final case class LogInHttpAnswer(userId: Long, token: String) extends HttpAnswer

final case class SendRoomMessageHttpAnswer(messageId: Long) extends HttpAnswer

object HttpAnswer {
  implicit val errorHttpAnswerEncodeJson = EncodeJson { (answer: ErrorHttpAnswer) =>
    ("code" := answer.code) ->:
      ("msg" := answer.msg) ->:
      jEmptyObject
  }
  implicit val signInHttpAnswerEncodeJson = EncodeJson { (answer: SignInHttpAnswer) =>
    ("userId" := answer.userId) ->:
      ("token" := answer.token) ->:
      jEmptyObject
  }
  implicit val logInHttpAnswerEncodeJson = EncodeJson { (answer: LogInHttpAnswer) =>
    ("userId" := answer.userId) ->:
      ("token" := answer.token) ->:
      jEmptyObject
  }
  implicit val sendRoomMessageHttpAnswerEncodeJson = EncodeJson { (answer: SendRoomMessageHttpAnswer) =>
    ("messageId" := answer.messageId) ->:
      jEmptyObject
  }
  implicit val httpAnswerEncodeJson = EncodeJson { (answer: HttpAnswer) =>
    answer match {
      case a: ErrorHttpAnswer           => ("answer" := "error") ->: errorHttpAnswerEncodeJson.encode(a)
      case a: SignInHttpAnswer          => ("answer" := "signIn") ->: signInHttpAnswerEncodeJson.encode(a)
      case a: LogInHttpAnswer           => ("answer" := "logIn") ->: logInHttpAnswerEncodeJson.encode(a)
      case a: SendRoomMessageHttpAnswer => ("answer" := "sendRoomMessage") ->: sendRoomMessageHttpAnswerEncodeJson.encode(a)
    }
  }
}
