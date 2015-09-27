package net.pointsgame.domain.services

import com.github.nscala_time.time.Imports._
import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import net.pointsgame.domain.model.User
import net.pointsgame.domain.repositories.UserRepository
import net.pointsgame.domain.{ Constants, DomainException }
import net.pointsgame.domain.helpers.{ Hasher, Validator }

final case class AccountService(userRepository: UserRepository, tokenService: TokenService) {
  def register(name: String, password: String): Task[(Long, String)] = Validator.checkUserName(name) {
    for {
      exists <- userRepository.existsWithName(name)
      userId <- if (exists) {
        Task.fail(new DomainException(s"User with name $name already exists."))
      } else {
        val salt = Hasher.generateSalt(Constants.saltLength)
        userRepository.insert(User(None, name, Hasher.hash(password, salt), salt, DateTime.now()))
      }
      token <- tokenService.create(userId)
    } yield (userId, token.tokenString)
  }
  def login(name: String, password: String): Task[(Long, String)] = Validator.checkUserName(name) {
    val result = for {
      user <- OptionT.optionT(userRepository.getByName(name))
      token <- (if (Hasher.hash(password, user.salt) sameElements user.passwordHash) {
        tokenService.create(user.id.get)
      } else {
        Task.fail(new DomainException(s"Wrong password."))
      }).liftM[OptionT]
    } yield (user.id.get, token.tokenString)
    result.getOrElseF(Task.fail(new DomainException("User with name doesn't exist.")))
  }
  def withUser[T](tokenString: String)(f: User => Task[T]): Task[T] = tokenService.withToken(tokenString) { token =>
    OptionT.optionT(userRepository.getById(token.userId))
      .flatMapF(f)
      .getOrElseF(Task.fail(new DomainException("User doesn't exist.")))
  }
}
