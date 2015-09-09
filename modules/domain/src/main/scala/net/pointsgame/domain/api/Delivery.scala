package net.pointsgame.domain.api

import argonaut._
import Argonaut._
import org.joda.time.DateTime

sealed trait Delivery

final case class ConnectedDelivery(id: String) extends Delivery

final case class RoomMessageDelivery(id: Int, roomId: Int, senderId: Int, body: String, date: DateTime) extends Delivery

object Delivery {
  implicit val connectedDeliveryEncodeJson = EncodeJson { (delivery: ConnectedDelivery) =>
    ("id" := delivery.id) ->: jEmptyObject
  }
  implicit val roomMessageDeliveryEncodeJson = EncodeJson { (delivery: RoomMessageDelivery) =>
    ("id" := delivery.id) ->: ("roomId" := delivery.roomId) ->: ("senderId" := delivery.senderId) ->: ("body" := delivery.body) /*->: ("date" := delivery.date)*/ ->: jEmptyObject
  }
  implicit val deliveryEncodeJson = EncodeJson { (delivery: Delivery) =>
    ("type" := "delivery") ->: (delivery match {
      case d: ConnectedDelivery   => ("delivery" := "connected") ->: connectedDeliveryEncodeJson.encode(d)
      case d: RoomMessageDelivery => ("delivery" := "roomMessage") ->: roomMessageDeliveryEncodeJson.encode(d)
    })
  }
}
