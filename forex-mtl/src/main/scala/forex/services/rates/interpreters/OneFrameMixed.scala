package forex.services.rates.interpreters

import cats.data.EitherT
import cats.effect.kernel.Async
import forex.domain.Rate
import forex.services.rates.{ errors, Algebra }

class OneFrameMixed[F[_]: Async](cache: OneFrameCache[F], live: OneFrameLive[F]) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] =
    EitherT(cache.get(pair))
      .orElse(EitherT(live.get(pair)).semiflatTap(cache.put))
      .value

}
