package net.pointsgame.domain.repositories

import scalaz.concurrent.Task
import net.pointsgame.domain.model.User

trait UserRepository extends Repository[User] {
  def getByName(name: String): Task[Option[User]]
  def existsWithName(name: String): Task[Boolean]
}
