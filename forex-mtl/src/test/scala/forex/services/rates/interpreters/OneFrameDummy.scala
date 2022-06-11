package forex.services.rates.interpreters

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.Algebra
import forex.services.rates.errors._
import forex.services.rates.interpreters.OneFrameDummy.dummyRate

import java.time.OffsetDateTime

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    dummyRate(pair).asRight[Error].pure[F]

}

object OneFrameDummy {

  val DummyPrice = Price(BigDecimal(100))

  val DummyTime = Timestamp(OffsetDateTime.parse("2022-06-08T22:08:44.621Z"))

  def dummyRate(pair: Rate.Pair) = Rate(pair, DummyPrice, DummyTime)
}