package forex.services.rates.interpreters

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.errors.Error.NotFound
import forex.services.rates.interpreters.TestData.{`JPY/USD`, `USD/JPY`}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class OneFrameCacheSpec extends AnyFlatSpec {

  it should "consider valid a rate with age < expiry" in {

    val now = OffsetDateTime.parse("2022-06-08T22:08:44.621Z")

    val rate = Rate(`USD/JPY`, Price(1.0), Timestamp(now))

    OneFrameCache.invalidate(1.minute, now)(rate) shouldBe Right(rate)
  }

  it should "consider valid a rate with age == expiry" in {

    val now = OffsetDateTime.parse("2022-06-08T22:08:44.621Z")

    val rate = Rate(`USD/JPY`, Price(1.0), Timestamp(now.minusMinutes((1))))

    OneFrameCache.invalidate(1.minute, now)(rate) shouldBe Right(rate)
  }

  it should "consider invalid a rate with age > expiry" in {

    val now = OffsetDateTime.parse("2022-06-08T22:08:44.621Z")

    val rate =
      Rate(`USD/JPY`, Price(1.0), Timestamp(now.minusMinutes(1).minus(1, ChronoUnit.MILLIS)))

    OneFrameCache.invalidate(1.minute, now)(rate) shouldBe
      Left(NotFound("Pair `USD/JPY` rate expired: `60001 ms` > `60000 ms`"))
  }

  it should "fall back with inverse look up" in {

    val refTime = OffsetDateTime.parse("2022-06-08T22:08:44.621Z")

    val rate = Rate(`USD/JPY`, Price(100.0), Timestamp(refTime))

    (for {
      store <- Ref[IO].of(Map.empty[Rate.Pair, Rate] + (rate.pair -> rate))
      cache = new OneFrameCache[IO](() => refTime, 5.minutes, store)
      rate1 <- cache.get(`USD/JPY`)
      rate2 <- cache.get(`JPY/USD`)
    } yield {
      rate1 shouldBe Right(rate)
      rate2 shouldBe Right(rate.inverse())

    }).unsafeRunSync()
  }
}
