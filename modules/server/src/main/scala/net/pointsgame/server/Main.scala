package net.pointsgame.server

import java.nio.file.{ Paths, Files }
import scalaz.concurrent.Task
import scalaz.stream._
import scalaz.stream.async._
import scalaz._
import Scalaz._
import argonaut._
import Argonaut._
import org.http4s.dsl._
import org.http4s.server._
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits._
import org.http4s.server.blaze._
import org.http4s.argonaut._
import slick.driver.SQLiteDriver.api._
import net.pointsgame.domain.{ Managers, Services, Oracle }
import net.pointsgame.db.repositories._
import net.pointsgame.db.schema._
import net.pointsgame.domain.managers._
import net.pointsgame.domain.services._
import net.pointsgame.domain.api._
import net.pointsgame.domain.model.Room

object Main extends App {
  val dbName = "pointsgame.db"

  val db = Database.forURL(s"jdbc:sqlite:$dbName", driver = "org.sqlite.JDBC")

  if (!Files.exists(Paths.get(dbName))) {
    val setup = DBIO.seq(
      (TableQuery[Users].schema ++
      TableQuery[Rooms].schema ++
      TableQuery[RoomMessages].schema ++
      TableQuery[Tokens].schema).create,
      TableQuery[Rooms] += Room(None, "main")
    )
    db.run(setup)
  }

  val userRepository = SlickUserRepository(db)
  val tokenRepository = SlickTokenRepository(db)
  val roomRepository = SlickRoomRepository(db)
  val roomMessageRepository = SlickRoomMessageRepository(db)

  val tokenService = TokenService(tokenRepository)
  val accountService = AccountService(userRepository, tokenService)
  val roomMessageService = RoomMessageService(roomMessageRepository, roomRepository, accountService)

  val services = Services(accountService, tokenService, roomMessageService)

  val connectionManager = new ConnectionManager
  val roomMessageManager = new RoomMessageManager(connectionManager, roomRepository)

  val managers = Managers(connectionManager, roomMessageManager)

  val oracle = Oracle(services, managers)

  implicit def httpQuestionDecoder[T <: HttpQuestion: DecodeJson] = jsonOf[T]
  implicit val httpAnswerEncoder = jsonEncoderOf[HttpAnswer]

  val route = HttpService {
    case req @ GET -> Root / "signIn" =>
      val params = req.params
      val answer = for {
        name <- params.get("name")
        password <- params.get("password")
      } yield oracle.answer(SignInHttpQuestion(name, password))
      answer.getOrElse(Task.now(ErrorHttpAnswer(0, "Invalid request!"))).flatMap(Ok(_))
    case req @ POST -> Root / "signIn" =>
      req.decode[SignInHttpQuestion] { question =>
        oracle.answer(question).flatMap(Ok(_))
      }
    case req @ GET -> Root / "logIn" =>
      val params = req.params
      val answer = for {
        name <- params.get("name")
        password <- params.get("password")
      } yield oracle.answer(LogInHttpQuestion(name, password))
      answer.getOrElse(Task.now(ErrorHttpAnswer(0, "Invalid request!"))).flatMap(Ok(_))
    case req @ POST -> Root / "logIn" =>
      req.decode[LogInHttpQuestion] { question =>
        oracle.answer(question).flatMap(Ok(_))
      }
    case req @ GET -> Root / "sendRoomMessage" =>
      val params = req.params
      val answer = for {
        token <- params.get("token")
        roomIdString <- params.get("roomId")
        roomId <- roomIdString.parseInt.toOption
        body <- params.get("body")
      } yield oracle.answer(SendRoomMessageHttpQuestion(token, roomId, body))
      answer.getOrElse(Task.now(ErrorHttpAnswer(0, "Invalid request!"))).flatMap(Ok(_))
    case req @ POST -> Root / "sendRoomMessage" =>
      req.decode[SendRoomMessageHttpQuestion] { question =>
        oracle.answer(question).flatMap(Ok(_))
      }
    case req @ POST -> Root / "question" =>
      req.decode[HttpQuestion] { question =>
        oracle.answer(question).flatMap(Ok(_))
      }
    case GET -> Root / "ws" =>
      val in = unboundedQueue[WebSocketFrame]
      val out = unboundedQueue[WebSocketFrame]
      val connectionId = oracle.newWS { delivery =>
        out.enqueueOne(Text(delivery.asJson.nospaces)).attemptRun
      }
      val answers = in.dequeue.collect {
        case Text(question, _) => Parse.decodeOption[WsQuestion](question)
      }.evalMap {
        case Some(question) => oracle.answer(connectionId, question)
        case None           => Task.now(ErrorWsAnswer(0, 0, "Invalid question format!"))
      }.map(answer => Text(answer.asJson.nospaces))
      val deliveries = out.dequeue
      val sink = in.enqueue.onComplete {
        Process.eval_(Task.delay(oracle.close(connectionId)))
      }
      WS(Exchange(answers merge deliveries, sink))
  }

  BlazeBuilder.bindHttp(8080)
    .withWebSockets(true)
    .mountService(route, "/api/v0.2/")
    .run
    .awaitShutdown()
}
