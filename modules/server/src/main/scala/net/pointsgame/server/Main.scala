package net.pointsgame.server

import java.nio.file.{ Paths, Files }
import slick.driver.SQLiteDriver.api._
import akka.actor.{ Props, ActorSystem }
import akka.io.IO
import spray.can.Http
import net.pointsgame.db.schema._
import net.pointsgame.db.repositories.{ SlickTokenRepository, SlickUserRepository }
import net.pointsgame.domain.{ Oracle, Services }
import net.pointsgame.domain.services.{ TokenService, AccountService }

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

  val accountService = AccountService(userRepository)
  val tokenService = TokenService(tokenRepository)

  val services = Services(accountService, tokenService)

  val oracle = new Oracle(services)

  val handler = system.actorOf(Props(classOf[ConnectionHandler], oracle), "handler")

  IO(Http) ! Http.Bind(handler, "localhost", 8080)
}
