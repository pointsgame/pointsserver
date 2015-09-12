package net.pointsgame.domain.api

import argonaut._
import Argonaut._
import scalaz._
import Scalaz._

case class TokenizedQuestion[+T <: Question](question: T, token: Option[String])

object TokenizedQuestion {
  implicit def registerQuestionDecodeJson[T <: Question : DecodeJson]: DecodeJson[TokenizedQuestion[T]] = DecodeJson { c =>
    ((c --\ "question").as[T] |@| (c --\ "token").as[String].option) { TokenizedQuestion.apply }
  }
}
