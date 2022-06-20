package forex.services.rates.refresh

import cats.data.EitherT
import cats.effect.Async
import cats.implicits.{catsSyntaxApply, toFunctorOps, toTraverseOps}
import forex.services.rates.errors
import forex.services.rates.interpreters.{OneFrameCache, OneFrameLive}

import scala.concurrent.duration.DurationInt

class CacheUpdater[F[_]: Async](oneFrameLive: OneFrameLive[F], oneFrameCache: OneFrameCache[F]) {

  def fill(): F[Either[errors.Error, Unit]] =
    EitherT(oneFrameLive.list()).semiflatTap(rates => rates.traverse(oneFrameCache.put)).void.value

  def update(): fs2.Stream[F, Unit] =
    fs2.Stream.repeatEval(fill() *> Async[F].sleep(2.minutes))
}
