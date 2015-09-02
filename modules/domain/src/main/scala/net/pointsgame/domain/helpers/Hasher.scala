package net.pointsgame.domain.helpers

import java.security.{ SecureRandom, MessageDigest }

object Hasher {
  private val random = new SecureRandom
  def hash(data: Array[Byte]): Array[Byte] = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(data)
    md.digest
  }
  def hash(str: String): Array[Byte] =
    hash(str.getBytes)
  def hash(data: Array[Byte], salt: Array[Byte]): Array[Byte] =
    hash(hash(data) ++ salt)
  def hash(str: String, salt: Array[Byte]): Array[Byte] =
    hash(str.getBytes, salt)
  def generateSalt(numBytes: Int): Array[Byte] =
    random.generateSeed(numBytes)
}
