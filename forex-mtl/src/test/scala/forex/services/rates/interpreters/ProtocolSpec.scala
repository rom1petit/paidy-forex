package forex.services.rates.interpreters

import forex.domain.Currency
import forex.services.rates.interpreters.Protocol.OneFrame
import io.circe.parser.parse
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.time.OffsetDateTime

class ProtocolSpec extends AnyFlatSpec {

  it should "decode OneFrame json response" in {
    val jsonStr =
      """
        |[
        |   {
        |      "from":"USD",
        |      "to":"JPY",
        |      "bid":0.14673449480981104,
        |      "ask":0.9145128676661823,
        |      "price":0.53062368123799667,
        |      "time_stamp":"2022-06-08T22:08:44.621Z"
        |   }
        |]""".stripMargin

    parse(jsonStr).flatMap(_.as[List[OneFrame]]) shouldBe
      Right(
        List(
          OneFrame(
            Currency.USD,
            Currency.JPY,
            BigDecimal(0.14673449480981104),
            BigDecimal(0.9145128676661823),
            BigDecimal("0.53062368123799667"),
            OffsetDateTime.parse("2022-06-08T22:08:44.621Z")
          )
        )
      )
  }
}
