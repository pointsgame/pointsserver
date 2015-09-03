package net.pointsgame.domain.helpers

import java.util.concurrent.ThreadLocalRandom
import scala.util.Random

object Tokenizer {
  def generate(numBytes: Int): String = {
    val random = new Random(ThreadLocalRandom.current())
    random.alphanumeric.take(numBytes).mkString
  }
}
