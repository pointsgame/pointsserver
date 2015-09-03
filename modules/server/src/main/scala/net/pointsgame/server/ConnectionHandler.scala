package net.pointsgame.server

import akka.actor.Props
import spray.can.Http
import spray.routing.HttpServiceActor
import net.pointsgame.domain.{ Services, Oracle }

final case class ConnectionHandler(services: Services) extends HttpServiceActor {
  def receive = {
    case Http.Connected(remoteAddress, localAddress) =>
      val serverConnection = sender()
      val oracle = new Oracle(services)
      val conn = context.actorOf(Props(classOf[MessageHandler], serverConnection, oracle))
      serverConnection ! Http.Register(conn)
  }
}
