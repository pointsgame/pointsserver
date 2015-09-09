package net.pointsgame.domain.helpers

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem

object Scheduler {
  private val actorSystem = ActorSystem("Scheduler")
  private val scheduler = actorSystem.scheduler
  def scheduleOnce(duration: FiniteDuration)(f: => Unit): Unit =
    scheduler.scheduleOnce(duration)(f)
}
