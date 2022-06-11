package forex.services.rates.interpreters

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref, Resource}
import forex.domain.Rate.Pair
import forex.domain.{Currency, Rate}
import forex.programs.emulator.OneFrameServiceEmulator
import forex.services.time.Clock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

class OneFrameMixedSpec extends AnyFlatSpec with OneFrameServiceEmulator {

  it should "successfully hit the cache when rate is not expired" in new Scope {

    val pair = Pair(Currency.USD, Currency.JPY)

    val data =
      oneFrameMixed
        .use(service => fs2.Stream.repeatEval(service.get(pair)).take(100).compile.toList)
        .unsafeRunSync()

    data.size shouldBe 100
    data.toSet shouldBe 1

  }

  trait Scope {

    val oneFrameMixed =
      oneFrameLive.flatMap { live =>
        for {
          cacheStorage <- Resource.eval(Ref[IO].of(Map.empty[Rate.Pair, Rate]))
        } yield {
          val cache = new OneFrameCache[IO](Clock(), 10.days, cacheStorage)
          new OneFrameMixed(cache, live)
        }
      }
  }
}
