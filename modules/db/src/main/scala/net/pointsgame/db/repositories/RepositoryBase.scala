package net.pointsgame.db.repositories

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.concurrent.Task
import slick.driver.SQLiteDriver.api._
import net.pointsgame.domain.model.Entity
import net.pointsgame.db.schema.BaseTable
import net.pointsgame.domain.repositories.Repository
import net.pointsgame.domain.helpers.ScalaScalaz._

abstract class RepositoryBase[T <: Entity, B <: BaseTable[T]](db: Database) extends Repository[T] {
  val query: TableQuery[B]
  override def all: Task[Seq[T]] = db.run {
    query.result
  }.asScalaz
  override def exists(id: Long): Task[Boolean] = db.run {
    query.filter(_.id === id).exists.result
  }.asScalaz
  override def getById(id: Long): Task[Option[T]] = db.run {
    query.filter(_.id === id).take(1).result.map(_.headOption)
  }.asScalaz
  override def deleteById(id: Long): Task[Boolean] = db.run {
    query.filter(_.id === id).take(1).delete.map(_ > 0)
  }.asScalaz
  override def insert(entity: T): Task[Long] = db.run {
    assert(entity.isNew)
    query.returning(query.map(_.id)) += entity
  }.asScalaz
}
