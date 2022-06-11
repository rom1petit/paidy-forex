package forex.services.rates.interpreters

import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class OneFrameCacheSpec extends AnyFlatSpec {

  it should "consider valid a rate with age < expiry" in {

    val now = OffsetDateTime.parse("2022-06-08T22:08:44.621Z")

    val rate = Rate(Pair(Currency.USD, Currency.JPY), Price(1.0), Timestamp(now))

    OneFrameCache.invalidate(1.minute, now)(rate) shouldBe Right(rate)
  }

  it should "consider valid a rate with age == expiry" in {

    val now = OffsetDateTime.parse("2022-06-08T22:08:44.621Z")

    val rate = Rate(Pair(Currency.USD, Currency.JPY), Price(1.0), Timestamp(now.minusMinutes((1))))

    OneFrameCache.invalidate(1.minute, now)(rate) shouldBe Right(rate)
  }

  it should "consider invalid a rate with age > expiry" in {

    val now = OffsetDateTime.parse("2022-06-08T22:08:44.621Z")

    val rate =
      Rate(Pair(Currency.USD, Currency.JPY), Price(1.0), Timestamp(now.minusMinutes(1).minus(1, ChronoUnit.MILLIS)))

    OneFrameCache.invalidate(1.minute, now)(rate) shouldBe
      Left(OneFrameLookupFailed("Pair `USD/JPY` rate expired: `60001 ms` > `60000 ms`"))
  }
}
