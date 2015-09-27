package net.pointsgame.domain.repositories

import scalaz.concurrent.Task
import net.pointsgame.domain.model.Entity

trait Repository[T <: Entity] {
  def all: Task[Seq[T]]
  def exists(id: Long): Task[Boolean]
  def getById(id: Long): Task[Option[T]]
  def deleteById(id: Long): Task[Boolean]
  def insert(entity: T): Task[Long]
}
