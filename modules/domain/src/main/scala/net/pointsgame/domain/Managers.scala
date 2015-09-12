package net.pointsgame.domain

import net.pointsgame.domain.managers._

case class Managers(connectionManager: ConnectionManager, roomMessageManager: RoomMessageManager)
