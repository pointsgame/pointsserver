package net.pointsgame.domain.api

sealed trait Delivery

case class Connected(id: Int) extends Delivery
