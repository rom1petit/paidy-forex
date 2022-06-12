package forex.services.rates.interpreters

import cats.Id
import cats.effect.unsafe.implicits.global
import forex.programs.emulator.OneFrameServiceEmulator
import forex.services.rates.interpreters.TestData.`USD/JPY`
import org.http4s.Uri
import org.scalatest.flatspec.AnyFlatSpec

class OneFrameLiveIntegrationSpec extends AnyFlatSpec with OneFrameServiceEmulator {

  it should "successfully build OneFrameService request" in {

    val actual = OneFrameLive
      .getPairRatesRequest[Id](
        Uri.unsafeFromString(s"http://localhost:8080"),
        "10dc303535874aeccc86a8251e6992f5",
        `USD/JPY`
      )

    actual.uri.toString() shouldBe "http://localhost:8080/rates?pair=USDJPY"
  }

  it should "successfully get USD/JPY rate from OneFrameService" in {

    oneFrameLive
      .use(service => service.get(`USD/JPY`))
      .map {
        case Left(value) => fail(s"Unexpected error $value")
        case Right(rate) => rate.pair shouldBe `USD/JPY`
      }
      .unsafeRunSync()
  }
}
