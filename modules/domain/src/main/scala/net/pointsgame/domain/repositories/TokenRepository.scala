package net.pointsgame.domain.repositories

import scala.concurrent.Future
import net.pointsgame.domain.model.Token

trait TokenRepository extends Repository[Token] {
  def getByTokenString(tokenString: String): Future[Option[Token]]
}
