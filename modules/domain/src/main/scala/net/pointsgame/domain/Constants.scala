package net.pointsgame.domain

import scala.concurrent.duration._

object Constants {
  val maxNameLength = 30
  val maxMessageLength = 30
  val saltLength = 32
  val tokenLength = 64
  val onlineTimeout = 3.seconds
}
