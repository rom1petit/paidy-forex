package forex.domain

import cats.Show
import cats.implicits.showInterpolator
import forex.programs.rates.errors.Error

import scala.math.BigDecimal.RoundingMode

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {

  private def inversePrice(price: Price) =
    Price((1 / price.value).setScale(5, RoundingMode.HALF_EVEN))

  implicit class RateOps(val rate: Rate) extends AnyVal {

    def inverse(): Rate =
      Rate(rate.pair.inverse(), inversePrice(rate.price), rate.timestamp)
  }

  /**
    * A currency pair is a price quote of the exchange rate for two different currencies
    * @param from the base currency
    * @param to the quote currency.
    */
  final case class Pair private (
      from: Currency,
      to: Currency
  )

  object Pair {
    /*
      override constructor to guarantee valid pair
     */
    def apply(from: Currency, to: Currency): Error Either Pair =
      if (from == to) Left(Error.IllegalArgument(s"A currency pair must be two different currencies."))
      else Right(new Pair(from, to))

    implicit class PairOps(val pair: Pair) extends AnyVal {

      def inverse(): Pair =
        new Pair(pair.to, pair.from)
    }
  }

  implicit val show: Show[Pair] = Show.show { pair =>
    show"${pair.from}/${pair.to}"
  }
}
