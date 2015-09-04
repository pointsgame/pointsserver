package net.pointsgame.domain.helpers

import scala.language.higherKinds
import scalaz._

object Hoists {
  implicit final class OptionHoistOps[A](option: Option[A]) {
    def hoist[M[+_]: Monad] = OptionT(Monad[M].point(option))
  }

  implicit final class EitherHoistOps[A, B](either: A \/ B) {
    def hoist[M[+_]: Monad] = EitherT(Monad[M].point(either))
  }
}
