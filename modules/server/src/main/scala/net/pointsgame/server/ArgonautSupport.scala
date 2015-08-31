package net.pointsgame.server

import argonaut._
import spray.http.{ ContentTypeRange, ContentTypes, HttpEntity, MediaTypes }
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.{ MalformedContent, SimpleUnmarshaller, Unmarshaller }

object ArgonautSupport {
  implicit def argonautMarshallerFromJson(implicit prettyPrinter: PrettyParams = PrettyParams.nospace): Marshaller[Json] =
    Marshaller.delegate[Json, String](ContentTypes.`application/json`) { jsValue =>
      prettyPrinter.pretty(jsValue)
    }
  implicit val argonautUnmarshallerToJson: Unmarshaller[Json] =
    new SimpleUnmarshaller[Json] {
      override val canUnmarshalFrom = Seq[ContentTypeRange](MediaTypes.`application/json`)
      override def unmarshal(entity: HttpEntity) = implicitly[Unmarshaller[String]].apply(entity).right.flatMap { str =>
        JsonParser.parse(str).leftMap(MalformedContent(_)).toEither
      }
    }
}
