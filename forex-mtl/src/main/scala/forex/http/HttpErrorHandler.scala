package forex.http

import cats.effect.Sync
import com.typesafe.scalalogging.StrictLogging
import forex.programs.rates.errors.Error.{IllegalArgument, RateLookupFailed}
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.server.ServiceErrorHandler

import scala.util.control.NonFatal

class HttpErrorHandler[F[_]: Sync] extends ServiceErrorHandler[F] with StrictLogging {

  override def apply(req: Request[F]): PartialFunction[Throwable, F[Response[F]]] =
    PartialFunction.fromFunction {
      case ex: IllegalArgument =>
        Sync[F].delay {
          logger.debug(s"Failed to handle $req", ex)
          Response[F](Status.BadRequest, req.httpVersion).withEntity(ex.msg.asJson)
        }

      case ex: RateLookupFailed =>
        Sync[F].delay {
          logger.error(s"Failed to handle $req", ex)
          Response[F](Status.NotFound, req.httpVersion).withEntity(ex.msg.asJson)
        }

      case ex: MessageFailure =>
        Sync[F].delay {
          logger.error(s"Failed to handle $req", ex)
          ex.toHttpResponse[F](req.httpVersion)
        }

      case NonFatal(ex) =>
        Sync[F].delay {
          logger.error(s"Failed to handle $req", ex)
          Response[F](Status.InternalServerError, req.httpVersion)
        }
    }
}
