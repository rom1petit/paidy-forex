package forex.services.rates

import cats.effect.kernel.Async
import forex.config.OneFrameClientConfig
import forex.services.rates.interpreters._
import org.http4s.client.Client

object Interpreters {

  def live[F[_]: Async](oneFrameClientConfig: OneFrameClientConfig, client: Client[F]): Algebra[F] =
    new OneFrameLive[F](oneFrameClientConfig, client)
}