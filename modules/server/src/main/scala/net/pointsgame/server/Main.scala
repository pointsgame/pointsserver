package net.pointsgame.server

import slick.driver.SQLiteDriver.api._
import akka.actor.{ Props, ActorSystem }
import akka.io.IO
import net.pointsgame.db.repositories.UserRepositoryImpl
import spray.can.Http
import net.pointsgame.domain.services.AccountService

object Main extends App {
  implicit val system = ActorSystem("server")

  val db = Database.forURL("jdbc:sqlite:pointsgame.db", driver = "org.sqlite.JDBC")

  val userRepository = UserRepositoryImpl(db)

  val accountService = AccountService(userRepository)

  val oracle = Oracle(accountService)

  val handler = system.actorOf(Props(classOf[ConnectionHandler], oracle), "handler")

  IO(Http) ! Http.Bind(handler, "localhost", 8080)
}
