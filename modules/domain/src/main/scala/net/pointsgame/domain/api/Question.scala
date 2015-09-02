package net.pointsgame.domain.api

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._

sealed trait Question {
  def qId: Option[String]
}

case class RegisterQuestion(qId: Option[String], name: String, password: String) extends Question //TODO: hashcash

case class LoginQuestion(qId: Option[String], name: String, password: String) extends Question

case class SendMessageQuestion(qId: Option[String], body: String) extends Question

case class SubscribeToRoomQuestion(qId: Option[String]) extends Question

case class UnsubscribeFromRoomQuestion(qId: Option[String]) extends Question

object Question {
  implicit val registerQuestionDecodeJson: DecodeJson[RegisterQuestion] = DecodeJson { c =>
    ((c --\ "qId").as[Option[String]] |@| (c --\ "name").as[String] |@| (c --\ "password").as[String]) { RegisterQuestion }
  }
  implicit val loginQuestionDecodeJson: DecodeJson[LoginQuestion] = DecodeJson { c =>
    ((c --\ "qId").as[Option[String]] |@| (c --\ "name").as[String] |@| (c --\ "password").as[String]) { LoginQuestion }
  }
  implicit val questionDecodeJson: DecodeJson[Question] =
    DecodeJson { c =>
      for {
        question <- (c --\ "question").as[String]
        result <- question match {
          case "register" => registerQuestionDecodeJson.decode(c)
          case "login"    => loginQuestionDecodeJson.decode(c)
          case _          => DecodeResult.fail("Unknown question type.", c.history)
        }
      } yield result
    }
}
