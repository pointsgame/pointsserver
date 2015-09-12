package net.pointsgame.server

import scala.concurrent.Future
import scalaz.concurrent.Task
import spray.httpx.marshalling.{ MarshallingContext, Marshaller }
import net.pointsgame.domain.helpers.ScalaScalaz._

object ScalazSupport {
  implicit def taskMarshaller[T](implicit marshaller: Marshaller[Future[T]]): Marshaller[Task[T]] = new Marshaller[Task[T]] {
    override def apply(value: Task[T], ctx: MarshallingContext): Unit =
      implicitly[Marshaller[Future[T]]].apply(value.asScala, ctx)
  }
}
