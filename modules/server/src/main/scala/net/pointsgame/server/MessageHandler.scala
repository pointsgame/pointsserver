package net.pointsgame.server

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import net.pointsgame.server.api.RegisterQuestion
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
      sender ! textFrame
    case frameCommandFailed: FrameCommandFailed =>
      log.error("Frame command failed.", frameCommandFailed)
    case httpRequest: HttpRequest =>
      businessLogicNoUpgrade(httpRequest)
    case _ =>
      send(TextFrame("Unrecognized event!"))
  }
  private val businessLogicNoUpgrade = runRoute {
    path("api" / "register") {
      get {
        parameters('name, 'password) { (name, password) =>
          complete {
            oracle.answer(RegisterQuestion(name, password))
          }
        }
      }
    }
  }
}
