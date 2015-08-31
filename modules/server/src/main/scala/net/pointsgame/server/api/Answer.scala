package net.pointsgame.server.api

import argonaut._
import Argonaut._

sealed trait Answer

case class ErrorAnswer(msg: String) extends Answer

case class RegisterAnswer(id: Int) extends Answer

object Answer {
  implicit val answerEncodeJson = EncodeJson { (answer: Answer) =>
    answer match {
      case ErrorAnswer(msg)   => ("type" := "error") ->: ("msg" := msg) ->: jEmptyObject
      case RegisterAnswer(id) => ("type" := "answer") ->: ("question" := "register") ->: ("id" := id) ->: jEmptyObject
    }
  }
}
