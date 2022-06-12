package forex.services.rates.interpreters

import cats.effect.kernel.Async
import cats.implicits._
import forex.config.OneFrameClientConfig
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.Algebra
import forex.services.rates.errors._
import forex.services.rates.interpreters.OneFrameLive.{convert, getPairRatesRequest, unique}
import forex.services.rates.interpreters.Protocol._
import org.http4s.Header.Raw
import org.http4s.Method.GET
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.{Headers, Request, Uri}
import org.typelevel.ci.CIString

class OneFrameLive[F[_]: Async](
    config: OneFrameClientConfig,
    client: Client[F]
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {

    val request = getPairRatesRequest[F](config.endpoint, config.token, pair)

    client.run(request).use { response =>
      response
        .as[List[OneFrame]]
        .map(unique(_, Error.OneFrameLookupFailed(show"Pair `$pair` rates not found")))
        .map(_.flatMap(convert))
    }
  }
}

object OneFrameLive {

  def convert(frame: OneFrame): Either[Error, Rate] =
    Rate
      .Pair(frame.from, frame.to)
      .leftMap(e => Error.OneFrameLookupFailed(e.getMessage))
      .map { p =>
        Rate(
          p,
          Price(frame.price),
          Timestamp(frame.timeStamp)
        )
      }

  def unique[A](values: Iterable[A], ifEmpty: Error): Error Either A =
    values.headOption.toRight(ifEmpty)

  def getPairRatesRequest[F[_]](base: Uri, token: String, pair: Rate.Pair): Request[F] = {

    val tokenHeader = Raw(CIString("token"), token)

    val uri = base / "rates" withQueryParam ("pair", pair)

    Request[F](GET, uri).withHeaders(Headers(tokenHeader))
  }

}
