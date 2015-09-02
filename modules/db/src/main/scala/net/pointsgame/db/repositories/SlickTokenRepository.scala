package net.pointsgame.db.repositories

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.SQLiteDriver.api._
import net.pointsgame.db.schema.Tokens
import net.pointsgame.domain.model.Token
import net.pointsgame.domain.repositories.TokenRepository

final case class SlickTokenRepository(db: Database) extends RepositoryBase[Token, Tokens](db) with TokenRepository {
  override val query: TableQuery[Tokens] =
    TableQuery[Tokens]
  override def getByTokenString(tokenString: String): Future[Option[Token]] = db.run {
    query.filter(_.tokenString === tokenString).take(1).result.map(_.headOption)
  }
}
