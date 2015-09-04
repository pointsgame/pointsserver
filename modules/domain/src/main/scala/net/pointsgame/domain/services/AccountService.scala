package net.pointsgame.domain.services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.nscala_time.time.Imports._
import scalaz._
import Scalaz._
import net.pointsgame.domain.model.User
import net.pointsgame.domain.repositories.UserRepository
import net.pointsgame.domain.{ Constants, DomainException }
import net.pointsgame.domain.helpers.Hasher
import net.pointsgame.domain.helpers.Hoists._

final class AccountService(userRepository: UserRepository, tokenService: TokenService) {
  def checkName[T](name: String)(f: => Future[T]): Future[T] = //TODO: check name characters.
    if (name.isEmpty)
      Future.failed(new DomainException("Name shouldn't be empty."))
    else if (name.length > Constants.maxNameLength)
      Future.failed(new DomainException(s"Name length should be not more than ${Constants.maxNameLength}."))
    else
      f
  def register(name: String, password: String): Future[(Int, String)] = checkName(name) {
    for {
      exists <- userRepository.existsWithName(name)
      userId <- if (exists) {
        Future.failed(new DomainException(s"User with name $name already exists."))
      } else {
        val salt = Hasher.generateSalt(Constants.saltLength)
        userRepository.insert(User(None, name, Hasher.hash(password, salt), salt, DateTime.now()))
      }
      token <- tokenService.create(userId)
    } yield (userId, token.tokenString)
  }
  def login(name: String, password: String): Future[(Int, String)] = checkName(name) {
    val result = for {
      user <- OptionT.optionT(userRepository.getByName(name))
      userId <- user.id.hoist[Future]
      token <- (if (Hasher.hash(password, user.salt) sameElements user.passwordHash) {
        tokenService.create(userId)
      } else {
        Future.failed(new DomainException(s"Wrong password."))
      }).liftM[OptionT]
    } yield (userId, token.tokenString)
    result.getOrElseF(Future.failed(new DomainException("User with name doesn't exist.")))
  }
  def withUser[T](tokenString: String)(f: User => Future[T]): Future[T] = tokenService.withToken(tokenString) { token =>
    OptionT.optionT(userRepository.getById(token.userId))
      .flatMapF(f)
      .getOrElseF(Future.failed(new DomainException("User doesn't exist.")))
  }
}
