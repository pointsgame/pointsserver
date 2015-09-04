package net.pointsgame.domain.api

import argonaut._
import Argonaut._

sealed trait Delivery

final case class ConnectedDelivery(id: String) extends Delivery

object Delivery {
  implicit val connectedDeliveryEncodeJson = EncodeJson { (delivery: ConnectedDelivery) =>
    ("id" := delivery.id) ->: jEmptyObject
  }
  implicit val deliveryEncodeJson = EncodeJson { (delivery: Delivery) =>
    delivery match {
      case d: ConnectedDelivery => connectedDeliveryEncodeJson.encode(d)
    }
  }
}
