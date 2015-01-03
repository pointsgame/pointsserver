package net.pointsgame.web_server

import akka.actor.ActorSystem
import akka.actor.Props
import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

object Main extends Logger {
  val actorSystem = ActorSystem("PointsGameActorSystem")

  val routes = Routes {
    case GET(request) =>
      actorSystem.actorOf(Props[HelloHandler]) ! request
  }

  def main(args: Array[String]) {
    val config = WebServerConfig(port = 8700)
    val webServer = new WebServer(config, routes, actorSystem)
    webServer.start()
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = webServer.stop()
    })
  }
}

import org.mashupbots.socko.events.HttpRequestEvent
import akka.actor.Actor
import java.util.Date

class HelloHandler extends Actor {
  def receive = {
    case event: HttpRequestEvent =>
      event.response.write("Hello from Socko (" + new Date().toString + ")")
      context.stop(self)
  }
}
