package forex.domain

import forex.services.rates.interpreters.TestData.{ `JPY/USD`, `USD/JPY` }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.time.OffsetDateTime

class RateSpec extends AnyFlatSpec {

  it should "compute inverse currency rate" in {
    val now = OffsetDateTime.parse("2022-06-08T22:08:44.621Z")

    val rate     = Rate(`USD/JPY`, Price(0.892343), Timestamp(now))
    val expected = Rate(`JPY/USD`, Price(1.12065), Timestamp(now))

    rate.inverse() shouldBe expected
  }
}
