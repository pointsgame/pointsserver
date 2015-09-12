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

object Main extends App {
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

  val tokenService = TokenService(tokenRepository)
  val accountService = AccountService(userRepository, tokenService)
  val roomMessageService = RoomMessageService(roomMessageRepository, roomRepository, accountService)

  val services = Services(accountService, tokenService, roomMessageService)

  val connectionManager = new ConnectionManager
  val roomMessageManager = new RoomMessageManager(roomRepository)

  val managers = Managers(connectionManager, roomMessageManager)

  def withOracle[T](f: Oracle => Task[T]): Task[T] =
    for {
      oracle <- Task.delay(new Oracle(services, managers))
      result <- f(oracle).onFinish(_ => Task.now(oracle.close()))
    } yield result

  implicit def questionDecoder[T <: Question: DecodeJson] = jsonOf[T]
  implicit val answerEncoder = jsonEncoderOf[Answer]

  val route = HttpService {
    case req @ GET -> Root / "register" =>
      val params = req.params
      val qId = params.get("qId")
      val token = params.get("token")
      val answer = for {
        name <- params.get("name")
        password <- params.get("password")
      } yield withOracle { oracle =>
        oracle.answer(RegisterQuestion(qId, token, name, password))
      }
      answer.getOrElse(Task.now(ErrorAnswer(qId, "Invalid request!"))).flatMap(Ok(_))
    case req @ POST -> Root / "register" =>
      req.decode[RegisterQuestion] { question =>
        withOracle { oracle =>
          oracle.answer(question).flatMap(Ok(_))
        }
      }
    case req @ GET -> Root / "login" =>
      val params = req.params
      val qId = params.get("qId")
      val token = params.get("token")
      val answer = for {
        name <- params.get("name")
        password <- params.get("password")
      } yield withOracle { oracle =>
        oracle.answer(LoginQuestion(qId, token, name, password))
      }
      answer.getOrElse(Task.now(ErrorAnswer(qId, "Invalid request!"))).flatMap(Ok(_))
    case req @ POST -> Root / "login" =>
      req.decode[LoginQuestion] { question =>
        withOracle { oracle =>
          oracle.answer(question).flatMap(Ok(_))
        }
      }
    case req @ GET -> Root / "sendRoomMessage" =>
      val params = req.params
      val qId = params.get("qId")
      val token = params.get("token")
      val answer = for {
        roomIdString <- params.get("roomId")
        roomId <- roomIdString.parseInt.toOption
        body <- params.get("body")
      } yield withOracle { oracle =>
        oracle.answer(SendRoomMessageQuestion(qId, token, roomId, body))
      }
      answer.getOrElse(Task.now(ErrorAnswer(qId, "Invalid request!"))).flatMap(Ok(_))
    case req @ POST -> Root / "sendRoomMessage" =>
      req.decode[SendRoomMessageQuestion] { question =>
        withOracle { oracle =>
          oracle.answer(question).flatMap(Ok(_))
        }
      }
    case req @ POST -> Root / "question" =>
      req.decode[Question] { question =>
        withOracle { oracle =>
          oracle.answer(question).flatMap(Ok(_))
        }
      }
    case GET -> Root / "ws" =>
      val oracle = new Oracle(services, managers)
      val in = unboundedQueue[WebSocketFrame]
      val out = unboundedQueue[WebSocketFrame]
      val answers = in.dequeue.collect {
        case Text(question, _) => Parse.decodeOption[Question](question)
      }.evalMap {
        case Some(question) => oracle.answer(question)
        case None           => Task.now(ErrorAnswer(None, "Invalid question format!"))
      }.map(answer => Text(answer.asJson.nospaces))
      oracle.setCallback { delivery =>
        out.enqueueOne(Text(delivery.asJson.nospaces)).attemptRun
      }
      val deliveries = out.dequeue
      val sink = in.enqueue.onComplete {
        Process.eval_(Task.delay(oracle.close()))
      }
      WS(Exchange(answers merge deliveries, sink))
  }

  BlazeBuilder.bindHttp(8080)
    .withWebSockets(true)
    .mountService(route, "/api/v0.1/")
    .run
    .awaitShutdown()
}
