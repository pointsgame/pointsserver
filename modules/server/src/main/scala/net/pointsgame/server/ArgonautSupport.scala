package net.pointsgame.server

import argonaut._
import Argonaut._
import shapeless._
import spray.http.{ ContentTypeRange, ContentTypes, HttpEntity, MediaTypes }
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.{ MalformedContent, SimpleUnmarshaller, Unmarshaller }

object ArgonautSupport {
  implicit def argonautMarshallerFromT[T: EncodeJson](implicit prettyPrinter: PrettyParams = PrettyParams.nospace, ev: T =:!= String): Marshaller[T] =
    Marshaller.delegate[T, String](ContentTypes.`application/json`) { value =>
      prettyPrinter.pretty(value.asJson)
    }
  implicit def argonautUnmarshallerToJson[T: DecodeJson]: Unmarshaller[T] =
    new SimpleUnmarshaller[T] {
      override val canUnmarshalFrom = Seq[ContentTypeRange](MediaTypes.`application/json`)
      override def unmarshal(entity: HttpEntity) = implicitly[Unmarshaller[String]].apply(entity).right.flatMap { str =>
        val result = for {
          x <- JsonParser.parse(str).leftMap(MalformedContent(_))
          y <- x.as[T].result.leftMap { case (msg, _) => MalformedContent(msg) }
        } yield y
        result.toEither
      }
    }
}
