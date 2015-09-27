package net.pointsgame.domain.services

import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.concurrent.Task
import net.pointsgame.domain.model.Token
import net.pointsgame.domain.repositories.TokenRepository
import net.pointsgame.domain.helpers.Tokenizer
import net.pointsgame.domain.{ DomainException, Constants }

final case class TokenService(tokenRepository: TokenRepository) {
  def create(userId: Long): Task[Token] = {
    val now = DateTime.now()
    val token = Token(None, userId, Tokenizer.generate(Constants.tokenLength), now, now, false)
    tokenRepository.insert(token).map(id => token.copy(id = Some(id)))
  }
  def withToken[T](tokenString: String)(f: Token => Task[T]): Task[T] =
    OptionT.optionT(tokenRepository.getByTokenString(tokenString))
      .flatMapF(f)
      .getOrElseF(Task.fail(new DomainException("Invalid token.")))
}
