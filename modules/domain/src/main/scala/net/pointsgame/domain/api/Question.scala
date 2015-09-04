package net.pointsgame.domain.api

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._

sealed trait Question {
  def qId: Option[String]
  def tokenOption: Option[String] =
    None
}

sealed trait QuestionWithToken extends Question {
  def token: String
  override def tokenOption: Option[String] =
    Some(token)
}

final case class RegisterQuestion(
  qId:      Option[String],
  name:     String,
  password: String
) extends Question

final case class LoginQuestion(
  qId:      Option[String],
  name:     String,
  password: String
) extends Question

final case class SendRoomMessageQuestion(
  qId:    Option[String],
  token:  String,
  roomId: Int,
  body:   String
) extends QuestionWithToken

/*
final case class SubscribeToRoomQuestion(
  qId: Option[String]
) extends Question

final case class UnsubscribeFromRoomQuestion(
  qId: Option[String]
) extends Question
*/

object Question {
  implicit val registerQuestionDecodeJson = DecodeJson { c =>
    (
      (c --\ "qId").as[String].option |@|
      (c --\ "name").as[String] |@|
      (c --\ "password").as[String]
    ) { RegisterQuestion }
  }
  implicit val loginQuestionDecodeJson = DecodeJson { c =>
    (
      (c --\ "qId").as[String].option |@|
      (c --\ "name").as[String] |@|
      (c --\ "password").as[String]
    ) { LoginQuestion }
  }
  implicit val sendRoomMessageQuestionDecodeJson = DecodeJson { c =>
    (
      (c --\ "qId").as[String].option |@|
      (c --\ "token").as[String] |@|
      (c --\ "roomId").as[Int] |@|
      (c --\ "body").as[String]
    ) { SendRoomMessageQuestion }
  }
  implicit val questionDecodeJson: DecodeJson[Question] =
    DecodeJson { c =>
      for {
        question <- (c --\ "question").as[String]
        result <- question match {
          case "register"        => registerQuestionDecodeJson.decode(c)
          case "login"           => loginQuestionDecodeJson.decode(c)
          case "sendRoomMessage" => sendRoomMessageQuestionDecodeJson.decode(c)
          case _                 => DecodeResult.fail("Unknown question type.", c.history)
        }
      } yield result
    }
}
