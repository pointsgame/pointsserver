package net.pointsgame.server

import akka.actor.Props
import spray.can.Http
import spray.routing.HttpServiceActor

final case class ConnectionHandler(oracle: Oracle) extends HttpServiceActor {
  def receive = {
    case Http.Connected(remoteAddress, localAddress) =>
      val serverConnection = sender()
      val conn = context.actorOf(Props(classOf[MessageHandler], serverConnection, oracle))
      serverConnection ! Http.Register(conn)
  }
}
