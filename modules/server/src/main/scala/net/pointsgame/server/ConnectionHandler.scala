package net.pointsgame.server

import akka.actor.Props
import spray.can.Http
import spray.routing.HttpServiceActor
import slick.driver.SQLiteDriver.api._
import net.pointsgame.domain.Oracle
import net.pointsgame.db.repositories.UserRepositoryImpl
import net.pointsgame.domain.services.AccountService

final case class ConnectionHandler(db: Database) extends HttpServiceActor {
  val userRepository = UserRepositoryImpl(db)

  val accountService = AccountService(userRepository)

  val oracle = new Oracle(accountService)

  def receive = {
    case Http.Connected(remoteAddress, localAddress) =>
      val serverConnection = sender()
      val conn = context.actorOf(Props(classOf[MessageHandler], serverConnection, oracle))
      serverConnection ! Http.Register(conn)
  }
}
