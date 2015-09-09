package net.pointsgame.server

import akka.actor.Props
import spray.can.Http
import spray.routing.HttpServiceActor
import net.pointsgame.domain.{ Oracle, Services }
import net.pointsgame.domain.managers.ConnectionManager

final class ConnectionHandler(services: Services, connectionManager: ConnectionManager) extends HttpServiceActor {
  def receive = {
    case Http.Connected(remoteAddress, localAddress) =>
      val serverConnection = sender()
      val oracle = new Oracle(services, connectionManager)
      val conn = context.actorOf(Props(classOf[MessageHandler], serverConnection, oracle))
      serverConnection ! Http.Register(conn)
  }
}
