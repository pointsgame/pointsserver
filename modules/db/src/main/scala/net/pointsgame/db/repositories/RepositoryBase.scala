package net.pointsgame.db.repositories

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.SQLiteDriver.api._
import net.pointsgame.domain.model.Entity
import net.pointsgame.db.schema.BaseTable
import net.pointsgame.domain.repositories.Repository

abstract class RepositoryBase[T <: Entity, B <: BaseTable[T]](db: Database) extends Repository[T] {
  val query: TableQuery[B]
  override def all: Future[Seq[T]] = db.run {
    query.result
  }
  override def exists(id: Int): Future[Boolean] = db.run {
    query.filter(_.id === id).exists.result
  }
  override def getById(id: Int): Future[Option[T]] = db.run {
    query.filter(_.id === id).take(1).result.map(_.headOption)
  }
  override def deleteById(id: Int): Future[Boolean] = db.run {
    query.filter(_.id === id).take(1).delete.map(_ > 0)
  }
  override def insert(entity: T): Future[Int] = db.run {
    assert(entity.isNew)
    query.returning(query.map(_.id)) += entity
  }
}
