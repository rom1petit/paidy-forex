package forex.services.rates.interpreters

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.Algebra
import forex.services.rates.errors._
import forex.services.rates.interpreters.OneFrameDummy.DummyPrice

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    Rate(pair, DummyPrice, Timestamp.now).asRight[Error].pure[F]

}

object OneFrameDummy {
  val DummyPrice = Price(BigDecimal(100))
}
