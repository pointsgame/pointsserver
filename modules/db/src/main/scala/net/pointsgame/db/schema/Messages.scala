package net.pointsgame.db.schema

import slick.driver.SQLiteDriver.api._

final class Messages(tag: Tag) extends BaseTable[(Int, String)](tag, "Rooms") {
  def roomId = column[Int]("RoomId")
  def senderId = column[Int]("SenderId")
  def body = column[String]("Body")
  def * = (id, body)
}
