package forex.domain

import cats.Show
import cats.implicits.showInterpolator
import forex.programs.rates.errors.Error

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {
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
  }

  implicit val show: Show[Pair] = Show.show { pair =>
    show"${pair.from}/${pair.to}"
  }
}
