package net.pointsgame.domain.helpers

import scala.concurrent.{ Promise, ExecutionContext, Future }
import scala.util.{ Failure, Success }
import scalaz._
import Scalaz._
import scalaz.concurrent.Task

object ScalaScalaz {
  implicit final class FutureOps[T](val future: Future[T]) extends AnyVal {
    def asScalaz(implicit ec: ExecutionContext): Task[T] =
      Task.async {
        register =>
          future.onComplete {
            case Success(v)  => register(v.right)
            case Failure(ex) => register(ex.left)
          }
      }
  }
  implicit final class TaskOps[T](val task: Task[T]) extends AnyVal {
    def asScala: Future[T] = {
      val promise = Promise[T]()
      task.runAsync {
        case -\/(ex) => promise.failure(ex)
        case \/-(r)  => promise.success(r)
      }
      promise.future
    }
  }
}
