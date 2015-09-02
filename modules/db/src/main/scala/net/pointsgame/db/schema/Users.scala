package net.pointsgame.db.schema

import com.github.nscala_time.time.Imports._
import slick.driver.SQLiteDriver.api._
import com.github.tototoshi.slick.SQLiteJodaSupport._
import net.pointsgame.domain.model.User
import net.pointsgame.domain.Constants

final class Users(tag: Tag) extends BaseTable[User](tag, "Users") {
  def name = column[String]("Name", O.Length(Constants.maxNameLength))
  def passwordHash = column[Array[Byte]]("PasswordHash")
  def salt = column[Array[Byte]]("Salt")
  def registrationDate = column[DateTime]("RegistrationDate")
  def uniqueName = index("IdxUserName", name, unique = true) //TODO: name.toLowerCase ?
  def * = (id.?, name, passwordHash, salt, registrationDate) <> (User.tupled, User.unapply)
}
