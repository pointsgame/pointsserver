package net.pointsgame.db.repositories

import slick.driver.SQLiteDriver.api._
import net.pointsgame.db.schema.RoomMessages
import net.pointsgame.domain.model.RoomMessage
import net.pointsgame.domain.repositories.RoomMessageRepository

final case class SlickRoomMessageRepository(db: Database) extends RepositoryBase[RoomMessage, RoomMessages](db) with RoomMessageRepository {
  override val query: TableQuery[RoomMessages] =
    TableQuery[RoomMessages]
}
