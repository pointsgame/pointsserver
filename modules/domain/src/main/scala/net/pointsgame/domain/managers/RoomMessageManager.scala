package net.pointsgame.domain.managers

import scala.collection.concurrent
import scalaz.concurrent.Task
import net.pointsgame.domain.repositories.RoomRepository
import net.pointsgame.domain.helpers.TrieMapUpdater._
import net.pointsgame.domain.DomainException

final class RoomMessageManager(roomRepository: RoomRepository) {
  private val subscribers = concurrent.TrieMap.empty[Int, Set[String]]
  def subscribe(roomId: Int, connectionId: String): Task[Unit] =
    for {
      exists <- if (subscribers.contains(roomId))
        Task.now(true)
      else
        roomRepository.exists(roomId)
      result <- if (exists) {
        if (subscribers.updateWithCond(roomId, Set.empty, s => if (s.contains(connectionId)) None else Some(s + connectionId)).isDefined) {
          Task.now(())
        } else {
          Task.fail(new DomainException("You are subscribed already."))
        }
      } else {
        Task.fail(new DomainException("Room with such id doesn't exist."))
      }
    } yield result
  def unsubscribe(roomId: Int, connectionId: String): Task[Unit] =
    if (subscribers.contains(roomId)) {
      if (subscribers.updateWithCond(roomId, Set.empty, s => if (s.contains(connectionId)) Some(s - connectionId) else None).isDefined) {
        Task.now(())
      } else {
        Task.fail(new DomainException("You doesn't subscribed."))
      }
    } else {
      Task.fail(new DomainException("Room with such id doesn't exist or you doesn't subscribed."))
    }
  def send(userId: Int, roomId: Int, body: String): Task[Int] = ???
}
