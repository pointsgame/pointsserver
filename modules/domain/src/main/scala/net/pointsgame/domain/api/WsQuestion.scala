package net.pointsgame.domain.api

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._

sealed trait WsQuestion {
  def qId: Long
}

final case class IntroducingWsQuestion(qId: Long, token: String) extends WsQuestion

final case class SubscribeToRoomWsQuestion(qId: Long, roomId: Long) extends WsQuestion

object WsQuestion {
  implicit val introducingWsQuestionDecodeJson = DecodeJson { c =>
    (
      (c --\ "qId").as[Long] |@|
      (c --\ "token").as[String]
    ) { IntroducingWsQuestion }
  }
  implicit val subscribeToRoomWsQuestionDecodeJson = DecodeJson { c =>
    (
      (c --\ "qId").as[Long] |@|
      (c --\ "roomId").as[Long]
    ) { SubscribeToRoomWsQuestion }
  }
  implicit val questionDecodeJson: DecodeJson[WsQuestion] =
    DecodeJson { c =>
      for {
        question <- (c --\ "question").as[String]
        result <- question match {
          case "introducing"     => introducingWsQuestionDecodeJson.decode(c)
          case "subscribeToRoom" => subscribeToRoomWsQuestionDecodeJson.decode(c)
          case _                 => DecodeResult.fail("Unknown question type.", c.history)
        }
      } yield result
    }
}
