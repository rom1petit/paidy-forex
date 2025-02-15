package forex.services.rates.interpreters

import cats.effect.Ref
import cats.effect.kernel.Async
import cats.implicits._
import forex.domain.Rate
import forex.services.rates.Algebra
import forex.services.rates.errors._
import forex.services.rates.interpreters.OneFrameCache.invalidate
import forex.services.rates.interpreters.OneFrameLive.unique
import forex.services.time.Clock

import java.time.{Duration, OffsetDateTime}
import scala.concurrent.duration.FiniteDuration
import scala.jdk.DurationConverters._

class OneFrameCache[F[_]: Async](
    clock: Clock,
    rateExpiry: FiniteDuration,
    cache: Ref[F, Map[Rate.Pair, Rate]]
) extends Algebra[F] {

  def put(rate: Rate): F[Unit] =
    cache.update(c => c + (rate.pair -> rate))

  private def fromInverse(store: Map[Rate.Pair, Rate], pair: Rate.Pair): Option[Rate] =
    store.get(pair.inverse()).map(_.inverse())

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    cache.get
      .map(store => store.get(pair).orElse(fromInverse(store, pair)))
      .map(unique(_, Error.NotFound(show"Pair `$pair` not found")))
      .map(_.flatMap(invalidate(rateExpiry, clock.now())))
}

object OneFrameCache {

  def invalidate(rateExpiry: FiniteDuration, now: OffsetDateTime)(rate: Rate): Error Either Rate = {
    val age = Duration.between(rate.timestamp.value, now).toScala

    if (age > rateExpiry) {
      Left(Error.NotFound(show"Pair `${rate.pair}` rate expired: `${age.toMillis} ms` > `${rateExpiry.toMillis} ms`"))
    } else {
      Right(rate)
    }
  }
}
