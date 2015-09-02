package net.pointsgame.db.schema

import com.github.nscala_time.time.Imports._
import slick.driver.SQLiteDriver.api._
import com.github.tototoshi.slick.SQLiteJodaSupport._
import net.pointsgame.domain.model.RoomMessage

final class RoomMessages(tag: Tag) extends BaseTable[RoomMessage](tag, "RoomMessages") {
  def body = column[String]("Body")
  def roomId = column[Int]("RoomId")
  def senderId = column[Int]("SenderId")
  def sendingDate = column[DateTime]("SendingDate")
  def roomIdForeignKey = foreignKey("RoomIdForeignKey", roomId, TableQuery[Rooms])(_.id)
  def senderIdForeignKey = foreignKey("SenderIdForeignKey", senderId, TableQuery[Users])(_.id)
  def * = (id.?, body, roomId, senderId, sendingDate) <> (RoomMessage.tupled, RoomMessage.unapply)
}
