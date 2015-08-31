package net.pointsgame.db.schema

import slick.driver.SQLiteDriver.api._

final class Rooms(tag: Tag) extends BaseTable[(Int, String)](tag, "Rooms") {
  def name = column[String]("Name")
  def uniqueName = index("idxUserName", name, unique = true)
  def * = (id, name)
}
