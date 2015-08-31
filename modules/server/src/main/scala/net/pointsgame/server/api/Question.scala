package net.pointsgame.server.api

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._

sealed trait Question

case class RegisterQuestion(name: String, password: String) extends Question

object Question {
  implicit val questionDecodeJson: DecodeJson[Question] =
    DecodeJson { c =>
      for {
        question <- (c --\ "question").as[String]
        result <- question match {
          case "register" => ((c --\ "name").as[String] |@| (c --\ "password").as[String]) { RegisterQuestion }
          case _          => DecodeResult.fail("Unknown question type.", c.history)
        }
      } yield result
    }
}
