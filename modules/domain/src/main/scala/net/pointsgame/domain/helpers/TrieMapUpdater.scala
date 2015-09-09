package net.pointsgame.domain.helpers

import scala.annotation.tailrec
import scala.collection.concurrent

object TrieMapUpdater {
  implicit final class RichTrieMap[K, V](val map: concurrent.TrieMap[K, V]) extends AnyVal {
    @tailrec
    def updateWith(key: K, default: => V, f: V => V): V = {
      val oldValue = map.getOrElseUpdate(key, default)
      val newValue = f(oldValue)
      if (!map.replace(key, oldValue, newValue))
        updateWith(key, default, f)
      else
        newValue
    }
    @tailrec
    def updateWithCond(key: K, default: => V, f: V => Option[V]): Option[V] = {
      val oldValue = map.getOrElseUpdate(key, default)
      f(oldValue) match {
        case Some(newValue) =>
          if (!map.replace(key, oldValue, newValue))
            updateWithCond(key, default, f)
          else
            Some(newValue)
        case None =>
          None
      }
    }
  }
}
