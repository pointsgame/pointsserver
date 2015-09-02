package net.pointsgame.db.schema

import slick.driver.SQLiteDriver.api._
import net.pointsgame.domain.model.Room

final class Rooms(tag: Tag) extends BaseTable[Room](tag, "Rooms") {
  def name = column[String]("Name")
  def uniqueName = index("IdxRoomName", name, unique = true)
  def * = (id.?, name) <> (Room.tupled, Room.unapply)
}
