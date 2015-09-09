package net.pointsgame.domain.managers

import scala.collection.concurrent
import net.pointsgame.domain.Constants
import net.pointsgame.domain.api.Delivery
import net.pointsgame.domain.helpers.Scheduler
import net.pointsgame.domain.helpers.TrieMapUpdater._

final class ConnectionManager {
  private val callbacks = concurrent.TrieMap.empty[String, Delivery => Unit]
  private val userIds = concurrent.TrieMap.empty[String, Int]
  private val connectionsCount = concurrent.TrieMap.empty[Int, Int]
  private def incCount(userId: Int): Unit =
    connectionsCount.updateWith(userId, 0, _ + 1)
  private def decCount(userId: Int): Unit = Scheduler.scheduleOnce(Constants.onlineTimeout) {
    connectionsCount.updateWith(userId, 1, _ - 1)
    connectionsCount.remove(userId, 0)
  }
  def putCallback(connectionId: String, callback: Delivery => Unit): Unit =
    callbacks += connectionId -> callback
  def putId(connectionId: String, userId: Int): Unit =
    userIds.put(connectionId, userId) match {
      case Some(oldUserId) =>
        if (oldUserId != userId) {
          decCount(oldUserId)
          incCount(userId)
        }
      case None =>
        incCount(userId)
    }
  def remove(connectionId: String): Unit = {
    callbacks -= connectionId
    for (userId <- userIds.remove(connectionId))
      decCount(userId)
  }
  def sendToConnection(connectionId: String, delivery: Delivery): Unit =
    for (f <- callbacks.get(connectionId))
      f(delivery)
  def sendToUser(userId: Int, delivery: Delivery): Unit =
    for {
      (connectionId, curUserId) <- userIds
      if curUserId == userId
      f <- callbacks.get(connectionId)
    } f(delivery)
  def online: Set[Int] =
    userIds.values.toSet
  def callbackExists(connectionId: String): Boolean =
    callbacks.contains(connectionId)
  def getId(connectionId: String): Option[Int] =
    userIds.get(connectionId)
}
