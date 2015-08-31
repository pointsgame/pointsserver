package net.pointsgame.db.repositories

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.SQLiteDriver.api._
import net.pointsgame.domain.model.User
import net.pointsgame.db.schema.Users
import net.pointsgame.domain.repositories.UserRepository

final case class UserRepositoryImpl(db: Database) extends RepositoryBase[User, Users](db) with UserRepository {
  override val query: TableQuery[Users] =
    TableQuery[Users]
  override def getByName(name: String): Future[Option[User]] = db.run {
    query.filter(_.name === name).take(1).result.map(_.headOption)
  }
  override def existsWithName(name: String): Future[Boolean] = db.run {
    query.filter(_.name === name).exists.result
  }
}
