package net.pointsgame.db.repositories

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.concurrent.Task
import slick.driver.SQLiteDriver.api._
import net.pointsgame.db.schema.Tokens
import net.pointsgame.domain.model.Token
import net.pointsgame.domain.repositories.TokenRepository
import net.pointsgame.domain.helpers.ScalaScalaz._

final case class SlickTokenRepository(db: Database) extends RepositoryBase[Token, Tokens](db) with TokenRepository {
  override val query: TableQuery[Tokens] =
    TableQuery[Tokens]
  override def getByTokenString(tokenString: String): Task[Option[Token]] = db.run {
    query.filter(_.tokenString === tokenString).take(1).result.map(_.headOption)
  }.asScalaz
}
