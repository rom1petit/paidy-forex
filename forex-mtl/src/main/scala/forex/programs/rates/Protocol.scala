package forex.programs.rates

import forex.domain.Rate.Pair

object Protocol {

  final case class GetRatesRequest(pair: Pair)

}
