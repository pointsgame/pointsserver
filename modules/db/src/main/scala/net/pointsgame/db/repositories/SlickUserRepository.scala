package net.pointsgame.db.repositories

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.concurrent.Task
import slick.driver.SQLiteDriver.api._
import net.pointsgame.domain.model.User
import net.pointsgame.db.schema.Users
import net.pointsgame.domain.repositories.UserRepository
import net.pointsgame.domain.helpers.ScalaScalaz._

final case class SlickUserRepository(db: Database) extends RepositoryBase[User, Users](db) with UserRepository {
  override val query: TableQuery[Users] =
    TableQuery[Users]
  override def getByName(name: String): Task[Option[User]] = db.run {
    query.filter(_.name === name).take(1).result.map(_.headOption)
  }.asScalaz
  override def existsWithName(name: String): Task[Boolean] = db.run {
    query.filter(_.name === name).exists.result
  }.asScalaz
}
