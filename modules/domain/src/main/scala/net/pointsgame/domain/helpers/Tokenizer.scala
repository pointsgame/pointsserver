package net.pointsgame.domain.helpers

import java.security.SecureRandom
import scala.util.Random

object Tokenizer {
  private val random = new Random(new SecureRandom)
  def generate(numBytes: Int): String =
    random.alphanumeric.take(numBytes).mkString
}
