package net.pointsgame.server

import net.pointsgame.domain.Oracle

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.pattern.pipe
import argonaut._
import Argonaut._
import net.pointsgame.domain.api.{ ErrorAnswer, Question, RegisterQuestion }
import spray.can.websocket.{ FrameCommandFailed, WebSocketServerWorker }
import spray.can.websocket.frame.{ TextFrame, BinaryFrame }
import spray.http.HttpRequest
import spray.routing.HttpServiceActor
import ArgonautSupport._

final case class MessageHandler(serverConnection: ActorRef, oracle: Oracle) extends HttpServiceActor with WebSocketServerWorker {
  override def receive =
    handshaking orElse businessLogicNoUpgrade orElse closeLogic
  override def businessLogic = {
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
        parameters('qid.?, 'name, 'password) { (qId, name, password) =>
          completeOracle(RegisterQuestion(qId, name, password))
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
