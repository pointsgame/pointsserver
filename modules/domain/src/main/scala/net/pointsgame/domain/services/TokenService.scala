package net.pointsgame.domain.services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.nscala_time.time.Imports._
import net.pointsgame.domain.model.Token
import net.pointsgame.domain.repositories.TokenRepository
import net.pointsgame.domain.helpers.Tokenizer
import net.pointsgame.domain.Constants

final case class TokenService(tokenRepository: TokenRepository) {
  def create(userId: Int): Future[Token] = {
    val now = DateTime.now()
    val token = Token(None, userId, Tokenizer.generate(Constants.tokenLength), now, now, false)
    tokenRepository.insert(token).map(id => token.copy(id = Some(id)))
  }
}
