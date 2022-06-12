package forex.http
package rates

import cats.implicits.toBifunctorOps
import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class GetApiRequest(
      from: Currency,
      to: Currency
  )

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { show.show _ andThen Json.fromString }
  implicit val currencyDecoder: Decoder[Currency] =
    Decoder[String].emap(s => Currency.fromString(s).leftMap(_.msg))

  implicit val pairEncoder: Encoder[Pair] =
    deriveConfiguredEncoder[Pair]
  implicit val pairDecoder: Decoder[Pair] = new Decoder[Pair] {
    final def apply(c: HCursor): Decoder.Result[Pair] =
      for {
        from <- c.downField("from").as[Currency]
        to <- c.downField("to").as[Currency]
        pair <- Pair(from, to).leftMap(err => DecodingFailure(err.getMessage, Nil))
      } yield {
        pair
      }
  }

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]
  implicit val rateDecoder: Decoder[Rate] =
    deriveConfiguredDecoder[Rate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]
  implicit val responseDecoder: Decoder[GetApiResponse] =
    deriveConfiguredDecoder[GetApiResponse]

}
