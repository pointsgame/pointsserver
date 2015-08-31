package net.pointsgame.domain.repositories

import scala.concurrent.Future
import net.pointsgame.domain.model.Entity

trait Repository[T <: Entity] {
  def getById(id: Int): Future[Option[T]]
  def deleteById(id: Int): Future[Boolean]
  def insert(entity: T): Future[Int]
}
