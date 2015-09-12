package net.pointsgame.domain.repositories

import scalaz.concurrent.Task
import net.pointsgame.domain.model.Entity

trait Repository[T <: Entity] {
  def all: Task[Seq[T]]
  def exists(id: Int): Task[Boolean]
  def getById(id: Int): Task[Option[T]]
  def deleteById(id: Int): Task[Boolean]
  def insert(entity: T): Task[Int]
}
