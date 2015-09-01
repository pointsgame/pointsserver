package net.pointsgame.server

import slick.driver.SQLiteDriver.api._
import akka.actor.{ Props, ActorSystem }
import akka.io.IO
import spray.can.Http

object Main extends App {
  implicit val system = ActorSystem("server")

  val db = Database.forURL("jdbc:sqlite:pointsgame.db", driver = "org.sqlite.JDBC")

  val handler = system.actorOf(Props(classOf[ConnectionHandler], db), "handler")

  IO(Http) ! Http.Bind(handler, "localhost", 8080)
}
