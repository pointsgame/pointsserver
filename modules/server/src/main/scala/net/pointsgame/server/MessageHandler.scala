package net.pointsgame.server

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.pattern.pipe
import scalaz._
import Scalaz._
import argonaut._
import Argonaut._
import spray.can.Http
import spray.can.websocket.{ FrameCommandFailed, WebSocketServerWorker, UpgradedToWebSocket }
import spray.can.websocket.frame.{ TextFrame, BinaryFrame }
import spray.http.HttpRequest
import spray.routing.HttpServiceActor
import ArgonautSupport._
import net.pointsgame.domain.api._
import net.pointsgame.domain.{ Constants, Oracle }
import net.pointsgame.domain.helpers.Tokenizer

final class MessageHandler(val serverConnection: ActorRef, oracle: Oracle) extends HttpServiceActor with WebSocketServerWorker {
  lazy val connectionId = Tokenizer.generate(Constants.connectionIdLength)
  override def receive =
    handshaking orElse businessLogicNoUpgrade orElse closeLogic
  override def businessLogic = {
    case UpgradedToWebSocket =>
      oracle.connect(connectionId, delivery => send(TextFrame(delivery.asJson.nospaces)))
    case _: BinaryFrame =>
      sender ! TextFrame("Binary frames are not supported!")
    case textFrame: TextFrame =>
      Parse.decodeOption[Question](textFrame.payload.utf8String).map(oracle.answer).getOrElse {
        Future.successful(ErrorAnswer(None, "Invalid question format!"))
      }.map(_.asJson.nospaces) pipeTo sender
    case frameCommandFailed: FrameCommandFailed =>
      log.error("Frame command failed.", frameCommandFailed)
    case httpRequest: HttpRequest =>
      businessLogicNoUpgrade(httpRequest)
    case _ =>
      send(TextFrame("Unrecognized event!"))
  }
  override def closeLogic = {
    case ev: Http.ConnectionClosed =>
      oracle.disconnect(connectionId)
      context.stop(self)
      log.debug("Connection closed on event: {}", ev)
  }
  private val prefix = "api"
  private val businessLogicNoUpgrade = runRoute {
    path(prefix / "register") {
      get {
        parameters('qId.?, 'name, 'password) { (qId, name, password) =>
          complete {
            oracle.register(qId, name, password)
          }
        }
      } ~
        post {
          entity(as[RegisterQuestion]) { question =>
            complete {
              oracle.register(question.qId, question.name, question.password)
            }
          }
        }
    } ~
      path(prefix / "login") {
        get {
          parameters('qId.?, 'name, 'password) { (qId, name, password) =>
            complete {
              oracle.login(qId, name, password)
            }
          }
        } ~
          post {
            entity(as[LoginQuestion]) { question =>
              complete {
                oracle.login(question.qId, question.name, question.password)
              }
            }
          }
      } ~
      path(prefix / "sendRoomMessage") {
        get {
          parameters('qId.?, 'token, 'roomId, 'body) { (qId, token, roomIdString, body) =>
            complete {
              roomIdString.parseInt.map(oracle.sendRoomMessage(qId, token, _, body))
                .getOrElse(Future.successful(ErrorAnswer(qId, "roomId shuld be a number."))): Future[Answer]
            }
          }
        } ~
          post {
            entity(as[SendRoomMessageQuestion]) { question =>
              complete {
                oracle.sendRoomMessage(question.qId, question.token, question.roomId, question.body)
              }
            }
          }
      } ~
      path(prefix / "question") {
        post {
          entity(as[Question]) { question =>
            complete {
              oracle.answer(question)
            }
          }
        }
      }
  }
}
