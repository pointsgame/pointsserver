package net.pointsgame.server

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.concurrent.Task
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
import ScalazSupport._
import net.pointsgame.domain.api._
import net.pointsgame.domain.Oracle
import net.pointsgame.domain.helpers.ScalaScalaz._

final class MessageHandler(val serverConnection: ActorRef, oracle: Oracle) extends HttpServiceActor with WebSocketServerWorker {
  override def receive =
    handshaking orElse businessLogicNoUpgrade orElse closeLogic
  override def businessLogic = {
    case UpgradedToWebSocket =>
      oracle.setCallback(delivery => send(TextFrame(delivery.asJson.nospaces)))
    case _: BinaryFrame =>
      sender ! TextFrame("Binary frames are not supported!")
    case textFrame: TextFrame =>
      Parse.decodeOption[Question](textFrame.payload.utf8String).map(oracle.answer).getOrElse {
        Task.now(ErrorAnswer(None, "Invalid question format!"))
      }.map(_.asJson.nospaces).asScala pipeTo sender
    case frameCommandFailed: FrameCommandFailed =>
      log.error("Frame command failed.", frameCommandFailed)
    case httpRequest: HttpRequest =>
      businessLogicNoUpgrade(httpRequest)
    case _ =>
      send(TextFrame("Unrecognized event!"))
  }
  override def closeLogic = {
    case ev: Http.ConnectionClosed =>
      context.stop(self)
      oracle.close()
      log.debug("Connection closed on event: {}", ev)
  }
  private val prefix = "api"
  private val businessLogicNoUpgrade = runRoute {
    path(prefix / "register") {
      get {
        parameters('qId.?, 'token.?, 'name, 'password) { (qId, token, name, password) =>
          complete {
            oracle.answer(RegisterQuestion(qId, token, name, password))
          }
        }
      } ~
        post {
          entity(as[RegisterQuestion]) { question =>
            complete {
              oracle.answer(question)
            }
          }
        }
    } ~
      path(prefix / "login") {
        get {
          parameters('qId.?, 'token.?, 'name, 'password) { (qId, token, name, password) =>
            complete {
              oracle.answer(LoginQuestion(qId, token, name, password))
            }
          }
        } ~
          post {
            entity(as[LoginQuestion]) { question =>
              complete {
                oracle.answer(question)
              }
            }
          }
      } ~
      path(prefix / "sendRoomMessage") {
        get {
          parameters('qId.?, 'token.?, 'roomId, 'body) { (qId, token, roomIdString, body) =>
            complete {
              roomIdString.parseInt.map(SendRoomMessageQuestion(qId, token, _, body))
                .map(oracle.answer _)
                .getOrElse(Task.now(ErrorAnswer(qId, "roomId shuld be a number."))): Task[Answer]
            }
          }
        } ~
          post {
            entity(as[SendRoomMessageQuestion]) { question =>
              complete {
                oracle.answer(question)
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
