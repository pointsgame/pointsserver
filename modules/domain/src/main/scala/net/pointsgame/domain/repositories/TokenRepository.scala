package net.pointsgame.domain.repositories

import scalaz.concurrent.Task
import net.pointsgame.domain.model.Token

trait TokenRepository extends Repository[Token] {
  def getByTokenString(tokenString: String): Task[Option[Token]]
}
