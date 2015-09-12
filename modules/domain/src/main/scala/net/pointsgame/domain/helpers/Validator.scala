package net.pointsgame.domain.helpers

import scalaz.concurrent.Task
import net.pointsgame.domain.{ Constants, DomainException }

object Validator { //TODO: check characters.
  def checkUserName[T](name: String)(f: => Task[T]): Task[T] =
    if (name.isEmpty)
      Task.fail(new DomainException("Name shouldn't be empty."))
    else if (name.length > Constants.maxNameLength)
      Task.fail(new DomainException(s"Name length should be not more than ${Constants.maxNameLength}."))
    else
      f
  def checkMessageBody[T](body: String)(f: => Task[T]): Task[T] =
    if (body.isEmpty)
      Task.fail(new DomainException("Body shouldn't be empty."))
    else if (body.length > Constants.maxMessageLength)
      Task.fail(new DomainException(s"Body length should be not more than ${Constants.maxMessageLength}."))
    else
      f
}
