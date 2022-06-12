package forex.http
package rates

import cats.Applicative
import cats.data.EitherT
import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeError, toFlatMapOps}
import forex.domain.{Currency, Rate}
import forex.http.rates.RatesHttpRoutes.EitherOps
import forex.http.security.BearerTokenAuth.BearerTokenHandler
import forex.programs.RatesProgram
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.Error
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}
import tsec.authentication.{TSecAuthService, _}

class RatesHttpRoutes[F[_]: Sync](security: BearerTokenHandler[F], rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._
  import Protocol._
  import QueryParams._

  private[http] val prefixPath = "/v1/rate"

  private val httpRoutes: HttpRoutes[F] = security
    .liftService(TSecAuthService {
      case GET -> Root :? FromQueryParam(fromParam) +& ToQueryParam(toParam) asAuthed _ =>
        (for {
          from <- Currency.fromString(fromParam).eitherT[F]
          to <- Currency.fromString(toParam).eitherT[F]
          pair <- Rate.Pair(from, to).eitherT[F]
          rate <- EitherT(rates.get(GetRatesRequest(pair)))
          response <- EitherT.right[Error](Ok(rate.asGetApiResponse))
        } yield {
          response
        }).value.flatMap(Sync[F].fromEither).handleErrorWith(rateError)
    })

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

  /*
    translate app error to http error
   */
  def rateError: Throwable => F[Response[F]] = {
    case e: Error.RateLookupFailed => NotFound(e.msg)
    case e: Error.IllegalArgument  => BadRequest(e.msg)
    case e                         => InternalServerError(e.getMessage)
  }
}

object RatesHttpRoutes {

  implicit class EitherOps[A, B](val either: Either[A, B]) {
    def eitherT[F[_]: Applicative]: EitherT[F, A, B] =
      EitherT.fromEither[F](either)
  }
}
