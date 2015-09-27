package net.pointsgame.domain.managers

import java.util.concurrent.ThreadLocalRandom
import scala.annotation.tailrec
import scala.collection.concurrent
import net.pointsgame.domain.Constants
import net.pointsgame.domain.helpers.Scheduler
import net.pointsgame.domain.helpers.TrieMapUpdater._
import net.pointsgame.domain.api.WsDelivery

final class ConnectionManager {
  private val callbacks = concurrent.TrieMap.empty[Long, WsDelivery => Unit]
  private val userIds = concurrent.TrieMap.empty[Long, Long]
  private val connectionsCount = concurrent.TrieMap.empty[Long, Long]
  private def incCount(userId: Long): Unit =
    connectionsCount.updateWith(userId, 0, _ + 1)
  private def decCount(userId: Long): Unit = Scheduler.scheduleOnce(Constants.onlineTimeout) {
    connectionsCount.updateWith(userId, 1, _ - 1)
    connectionsCount.remove(userId, 0)
  }
  def newConnection(callback: WsDelivery => Unit): Long = {
    @tailrec
    def putConnection(id: Long): Long =
      if (callbacks.putIfAbsent(id, callback).isEmpty)
        id
      else
        putConnection(id + 1)
    val id = ThreadLocalRandom.current.nextLong()
    putConnection(id)
  }
  def updateUserId(connectionId: Long, userId: Long): Unit =
    userIds.put(connectionId, userId) match {
      case Some(oldUserId) =>
        if (oldUserId != userId) {
          decCount(oldUserId)
          incCount(userId)
        }
      case None =>
        incCount(userId)
    }
  def wasSeenUserId(userId: Long): Unit = {
    incCount(userId)
    decCount(userId)
  }
  def remove(connectionId: Long): Unit = {
    callbacks -= connectionId
    for (userId <- userIds.remove(connectionId))
      decCount(userId)
  }
  def sendToConnection(connectionId: Long, delivery: WsDelivery): Unit =
    for (f <- callbacks.get(connectionId))
      f(delivery)
  def sendToUser(userId: Long, delivery: WsDelivery): Unit =
    for {
      (connectionId, curUserId) <- userIds
      if curUserId == userId
      f <- callbacks.get(connectionId)
    } f(delivery)
  def online: Set[Long] =
    userIds.values.toSet
  def connectionExists(connectionId: Long): Boolean =
    callbacks.contains(connectionId)
  def getId(connectionId: Long): Option[Long] =
    userIds.get(connectionId)
}
