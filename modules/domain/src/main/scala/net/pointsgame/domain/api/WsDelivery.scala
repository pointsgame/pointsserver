package net.pointsgame.domain.api

import argonaut._
import Argonaut._
import org.joda.time.DateTime

sealed trait WsDelivery

final case class RoomMessageWsDelivery(messageId: Long, roomId: Long, senderId: Long, body: String, sendingDate: DateTime) extends WsDelivery

object WsDelivery {
  implicit val roomMessageDeliveryEncodeJson = EncodeJson { (delivery: RoomMessageWsDelivery) =>
    ("messageId" := delivery.messageId) ->:
      ("roomId" := delivery.roomId) ->:
      ("senderId" := delivery.senderId) ->:
      ("body" := delivery.body) ->:
      jEmptyObject
  }
  implicit val wsDeliveryEncodeJson = EncodeJson { (delivery: WsDelivery) =>
    ("type" := "delivery") ->: (delivery match {
      case d: RoomMessageWsDelivery => ("delivery" := "roomMessage") ->: roomMessageDeliveryEncodeJson.encode(d)
    })
  }
}
