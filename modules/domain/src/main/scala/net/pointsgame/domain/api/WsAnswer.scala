package net.pointsgame.domain.api

import argonaut._
import Argonaut._

sealed trait WsAnswer {
  def qId: Long
}

final case class ErrorWsAnswer(qId: Long, code: Long, msg: String) extends WsAnswer

final case class IntroducingWsAnswer(qId: Long) extends WsAnswer

final case class SubscribeToRoomWsAnswer(qId: Long, roomId: Long) extends WsAnswer

object WsAnswer {
  implicit val errorWsAnswerEncodeJson = EncodeJson { (answer: ErrorWsAnswer) =>
    ("qId" := answer.qId) ->:
      ("code" := answer.code) ->:
      jEmptyObject
  }
  implicit val introducingWsAnswerEncodeJson = EncodeJson { (answer: IntroducingWsAnswer) =>
    ("qId" := answer.qId) ->:
      jEmptyObject
  }
  implicit val subscribeToRoomWsAnswerEncodeJson = EncodeJson { (answer: SubscribeToRoomWsAnswer) =>
    ("qId" := answer.qId) ->:
      ("roomId" := answer.roomId) ->:
      jEmptyObject
  }
  implicit val wsAnswerEncodeJson = EncodeJson { (answer: WsAnswer) =>
    ("type" := "answer") ->: (answer match {
      case a: ErrorWsAnswer           => ("answer" := "error") ->: errorWsAnswerEncodeJson.encode(a)
      case a: IntroducingWsAnswer     => ("answer" := "introducing") ->: introducingWsAnswerEncodeJson.encode(a)
      case a: SubscribeToRoomWsAnswer => ("answer" := "subscribeToRoom") ->: subscribeToRoomWsAnswerEncodeJson.encode(a)
    })
  }
}
