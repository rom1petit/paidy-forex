package forex.services.rates.interpreters

import cats.effect.unsafe.implicits.global
import cats.effect.{ IO, Ref, Resource }
import forex.domain.Rate.Pair
import forex.domain.{ Currency, Rate }
import forex.programs.emulator.OneFrameServiceEmulator
import forex.services.time.Clock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.duration.FiniteDuration

class OneFrameMixedIntegrationSpec extends AnyFlatSpec with OneFrameServiceEmulator {

  it should "successfully hit the cache when rate is not expired" in new Scope {

    override def expiry: FiniteDuration = 5.minutes

    val pair = Pair(Currency.USD, Currency.JPY)

    val data =
      oneFrameMixed
        .use(service => fs2.Stream.repeatEval(service.get(pair)).take(100).compile.toList)
        .unsafeRunSync()

    data.size shouldBe 100
    data.toSet.size shouldBe 1

  }

  trait Scope {

    def expiry: FiniteDuration

    val oneFrameMixed =
      oneFrameLive.flatMap { live =>
        for {
          cacheStorage <- Resource.eval(Ref[IO].of(Map.empty[Rate.Pair, Rate]))
        } yield {
          val cache = new OneFrameCache[IO](Clock(), expiry, cacheStorage)
          new OneFrameMixed(cache, live)
        }
      }
  }
}
