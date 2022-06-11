package forex.services.rates

import cats.effect.Ref
import cats.effect.kernel.Async
import forex.config.OneFrameClientConfig
import forex.domain.Rate
import forex.services.rates.interpreters._
import forex.services.time.Clock
import org.http4s.client.Client

object Interpreters {

  def cache[F[_]: Async](clock: Clock, ref: Ref[F, Map[Rate.Pair, Rate]]): Algebra[F] =
    new OneFrameCache[F](clock, ref)

  def live[F[_]: Async](oneFrameClientConfig: OneFrameClientConfig, client: Client[F]): Algebra[F] =
    new OneFrameLive[F](oneFrameClientConfig, client)

  def mixed[F[_]: Async](clock: Clock,
                         ref: Ref[F, Map[Rate.Pair, Rate]],
                         oneFrameClientConfig: OneFrameClientConfig,
                         client: Client[F]): Algebra[F] =
    new OneFrameMixed[F](
      new OneFrameCache[F](clock, ref),
      new OneFrameLive[F](oneFrameClientConfig, client)
    )
}
