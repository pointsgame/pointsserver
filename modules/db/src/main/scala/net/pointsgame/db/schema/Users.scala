package net.pointsgame.db.schema

import slick.driver.SQLiteDriver.api._
import net.pointsgame.domain.model.User
import net.pointsgame.domain.Constants

final class Users(tag: Tag) extends BaseTable[User](tag, "Users") {
  def name = column[String]("Name", O.Length(Constants.maxNameLength))
  def uniqueName = index("idxUserName", name, unique = true)
  def * = (id.?, name) <> (User.tupled, User.unapply)
}
