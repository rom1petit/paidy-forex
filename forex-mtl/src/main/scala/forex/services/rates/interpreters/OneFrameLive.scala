package forex.services.rates.interpreters

import cats.effect.implicits.monadCancelOps
import cats.effect.kernel.{ Async, Outcome, Sync }
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import forex.config.OneFrameClientConfig
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.rates.Algebra
import forex.services.rates.errors._
import forex.services.rates.interpreters.OneFrameLive.{ convert, getPairRatesRequest, log, unique }
import forex.services.rates.interpreters.Protocol._
import org.http4s.Header.Raw
import org.http4s.Method.GET
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.{ Headers, Request, Uri }
import org.typelevel.ci.CIString

class OneFrameLive[F[_]: Async](
    config: OneFrameClientConfig,
    client: Client[F]
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {

    val request = getPairRatesRequest[F](config.endpoint, config.token, pair)

    log(show"GET `$pair` rate from OneFrameService") {
      client.run(request).use { response =>
        response
          .as[List[OneFrame]]
          .map(unique(_, Error.NotFound(show"Pair `$pair` rates not found")))
          .map(_.flatMap(convert))
      }
    }.handleError(err => Error.OneFrameLookupFailed(err.getMessage).asLeft)
  }
}

object OneFrameLive extends StrictLogging {

  def log[F[_]: Async, A](msg: String)(fa: F[A]): F[A] =
    Sync[F].delay(logger.info(s"running $msg")) *>
      fa.guaranteeCase {
        case Outcome.Succeeded(_) =>
          Sync[F].delay(logger.info(s"Succeed to $msg"))
        case Outcome.Errored(e) =>
          Sync[F].delay(logger.error(s"Failed to $msg", e))
        case Outcome.Canceled() =>
          Sync[F].delay(logger.warn(s"$msg canceled"))
      }

  def convert(frame: OneFrame): Either[Error, Rate] =
    Rate
      .Pair(frame.from, frame.to)
      .leftMap(e => Error.NotFound(e.getMessage))
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
