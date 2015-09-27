package net.pointsgame.db.schema

import com.github.nscala_time.time.Imports._
import slick.driver.SQLiteDriver.api._
import com.github.tototoshi.slick.SQLiteJodaSupport._
import net.pointsgame.domain.model.Token

final class Tokens(tag: Tag) extends BaseTable[Token](tag, "Tokens") {
  def userId = column[Long]("UserId")
  def tokenString = column[String]("Token")
  def creationDate = column[DateTime]("CreationDate")
  def lastAccessDate = column[DateTime]("LastAccessDate")
  def expired = column[Boolean]("Expired")
  def uniqueTokenString = index("IdxTokenTokenString", tokenString, unique = true)
  def userIdForeignKey = foreignKey("UserIdForeignKey", userId, TableQuery[Users])(_.id)
  def * = (id.?, userId, tokenString, creationDate, lastAccessDate, expired) <> (Token.tupled, Token.unapply)
}
