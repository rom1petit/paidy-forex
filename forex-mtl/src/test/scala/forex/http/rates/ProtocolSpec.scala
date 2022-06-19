package forex.http.rates

import forex.domain.{Price, Timestamp}
import forex.http.rates.Protocol.GetApiResponse
import forex.services.rates.interpreters.TestData._
import io.circe.syntax.EncoderOps
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.time.OffsetDateTime

class ProtocolSpec extends AnyFlatSpec {

  it should "successfully encode valid json " in {

    val actual =
      GetApiResponse(
        `USD/JPY`.from,
        `USD/JPY`.to,
        Price(0.99999900000),
        Timestamp(OffsetDateTime.parse("2022-06-08T22:08:44.621Z"))
      )

    val expected =
      """{
        |  "from" : "USD",
        |  "to" : "JPY",
        |  "price" : 0.999999,
        |  "timestamp" : "2022-06-08T22:08:44.621Z"
        |}""".stripMargin

    actual.asJson.toString() shouldBe expected

  }
}
