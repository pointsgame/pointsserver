package net.pointsgame.db.schema

import slick.driver.SQLiteDriver.api._

abstract class BaseTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
}
