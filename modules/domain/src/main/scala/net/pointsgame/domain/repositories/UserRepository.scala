package net.pointsgame.domain.repositories

import scala.concurrent.Future
import net.pointsgame.domain.model.User

trait UserRepository extends Repository[User] {
  def getByName(name: String): Future[Option[User]]
  def existsWithName(name: String): Future[Boolean]
}
