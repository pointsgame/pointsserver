package net.pointsgame.server

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.pattern.pipe
import argonaut._
import Argonaut._
import spray.can.websocket.{ FrameCommandFailed, WebSocketServerWorker, UpgradedToWebSocket }
import spray.can.websocket.frame.{ TextFrame, BinaryFrame }
import spray.http.HttpRequest
import spray.routing.HttpServiceActor
import ArgonautSupport._
import net.pointsgame.domain.api.{ ErrorAnswer, Question, RegisterQuestion }
import net.pointsgame.domain.Oracle

final case class MessageHandler(serverConnection: ActorRef, oracle: Oracle) extends HttpServiceActor with WebSocketServerWorker {
  override def receive =
    handshaking orElse businessLogicNoUpgrade orElse closeLogic
  override def businessLogic = {
    case UpgradedToWebSocket =>
      oracle.delivery = delivery => send(TextFrame(delivery.toString)) //TODO: JSON
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
  private val prefix = "api"
  private def completeOracle(question: Question) = complete {
    oracle.answer(question)
  }
  private val businessLogicNoUpgrade = runRoute {
    path(prefix / "register") {
      get {
        parameters('qId.?, 'token.?, 'name, 'password) { (qId, token, name, password) =>
          completeOracle(RegisterQuestion(qId, token, name, password))
        }
      } ~
        post {
          entity(as[RegisterQuestion]) { question =>
            completeOracle(question)
          }
        }
    } ~
      path(prefix / "question") {
        post {
          entity(as[Question]) { question =>
            completeOracle(question)
          }
        }
      }
  }
}
