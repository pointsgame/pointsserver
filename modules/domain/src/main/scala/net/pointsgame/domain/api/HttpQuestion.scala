package net.pointsgame.domain.api

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._

sealed trait HttpQuestion

final case class SignInHttpQuestion(name: String, password: String) extends HttpQuestion

final case class LogInHttpQuestion(name: String, password: String) extends HttpQuestion

final case class SendRoomMessageHttpQuestion(token: String, roomId: Long, body: String) extends HttpQuestion

object HttpQuestion {
  implicit val signInHttpQuestionDecodeJson = DecodeJson { c =>
    (
      (c --\ "name").as[String] |@|
      (c --\ "password").as[String]
    ) { SignInHttpQuestion }
  }
  implicit val logInHttpQuestionDecodeJson = DecodeJson { c =>
    (
      (c --\ "name").as[String] |@|
      (c --\ "password").as[String]
    ) { LogInHttpQuestion }
  }
  implicit val sendRoomMessageHttpQuestionDecodeJson = DecodeJson { c =>
    (
      (c --\ "token").as[String] |@|
      (c --\ "roomId").as[Long] |@|
      (c --\ "body").as[String]
    ) { SendRoomMessageHttpQuestion }
  }
  implicit val questionDecodeJson: DecodeJson[HttpQuestion] =
    DecodeJson { c =>
      for {
        question <- (c --\ "question").as[String]
        result <- question match {
          case "signIn"          => signInHttpQuestionDecodeJson.decode(c)
          case "logIn"           => logInHttpQuestionDecodeJson.decode(c)
          case "sendRoomMessage" => sendRoomMessageHttpQuestionDecodeJson.decode(c)
          case _                 => DecodeResult.fail("Unknown question type.", c.history)
        }
      } yield result
    }
}
