package net.pointsgame.domain.api

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._

sealed trait Question {
  def qId: Option[String]
}

case class RegisterQuestion(qId: Option[String], name: String, password: String) extends Question

object Question {
  implicit val registerQuestionDecodeJson: DecodeJson[RegisterQuestion] = DecodeJson { c =>
    ((c --\ "qid").as[Option[String]] |@| (c --\ "name").as[String] |@| (c --\ "password").as[String]) { RegisterQuestion }
  }
  implicit val questionDecodeJson: DecodeJson[Question] =
    DecodeJson { c =>
      for {
        question <- (c --\ "question").as[String]
        result <- question match {
          case "register" => registerQuestionDecodeJson.decode(c)
          case _          => DecodeResult.fail("Unknown question type.", c.history)
        }
      } yield result
    }
}
