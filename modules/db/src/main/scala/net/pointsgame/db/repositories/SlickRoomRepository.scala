package net.pointsgame.db.repositories

import slick.driver.SQLiteDriver.api._
import net.pointsgame.db.schema.Rooms
import net.pointsgame.domain.model.Room
import net.pointsgame.domain.repositories.RoomRepository

final case class SlickRoomRepository(db: Database) extends RepositoryBase[Room, Rooms](db) with RoomRepository {
  override val query: TableQuery[Rooms] =
    TableQuery[Rooms]
}
