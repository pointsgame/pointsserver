package net.pointsgame.server

import java.nio.file.{ Paths, Files }
import net.pointsgame.domain.managers.ConnectionManager
import slick.driver.SQLiteDriver.api._
import akka.actor.{ Props, ActorSystem }
import akka.io.IO
import spray.can.Http
import net.pointsgame.db.schema._
import net.pointsgame.db.repositories._
import net.pointsgame.domain.Services
import net.pointsgame.domain.services._

object Main extends App {
  implicit val system = ActorSystem("server")

  val dbName = "pointsgame.db"

  val db = Database.forURL(s"jdbc:sqlite:$dbName", driver = "org.sqlite.JDBC")

  if (!Files.exists(Paths.get(dbName))) {
    val setup = DBIO seq {
      TableQuery[Users].schema ++
        TableQuery[Rooms].schema ++
        TableQuery[RoomMessages].schema ++
        TableQuery[Tokens].schema
    }.create
    db.run(setup)
  }

  val userRepository = SlickUserRepository(db)
  val tokenRepository = SlickTokenRepository(db)
  val roomRepository = SlickRoomRepository(db)
  val roomMessageRepository = SlickRoomMessageRepository(db)

  val tokenService = new TokenService(tokenRepository)
  val accountService = new AccountService(userRepository, tokenService)
  val roomMessageService = new RoomMessageService(roomMessageRepository, roomRepository, accountService)

  val services = Services(accountService, tokenService, roomMessageService)

  val connectionManager = new ConnectionManager

  val handler = system.actorOf(Props(classOf[ConnectionHandler], services, connectionManager), "handler")

  IO(Http) ! Http.Bind(handler, "localhost", 8080)
}
