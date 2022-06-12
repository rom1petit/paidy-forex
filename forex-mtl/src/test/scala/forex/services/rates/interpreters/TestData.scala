package forex.services.rates.interpreters

import forex.domain.Currency
import forex.domain.Rate.Pair

object TestData {

  val `USD/JPY`: Pair = Pair(Currency.USD, Currency.JPY).toOption.get

}
