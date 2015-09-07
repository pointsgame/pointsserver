package net.pointsgame.domain.helpers

import scala.concurrent.Future
import net.pointsgame.domain.{ Constants, DomainException }

object Validator { //TODO: check characters.
  def checkUserName[T](name: String)(f: => Future[T]): Future[T] =
    if (name.isEmpty)
      Future.failed(new DomainException("Name shouldn't be empty."))
    else if (name.length > Constants.maxNameLength)
      Future.failed(new DomainException(s"Name length should be not more than ${Constants.maxNameLength}."))
    else
      f
  def checkMessageBody[T](body: String)(f: => Future[T]): Future[T] =
    if (body.isEmpty)
      Future.failed(new DomainException("Body shouldn't be empty."))
    else if (body.length > Constants.maxMessageLength)
      Future.failed(new DomainException(s"Body length should be not more than ${Constants.maxMessageLength}."))
    else
      f
}
