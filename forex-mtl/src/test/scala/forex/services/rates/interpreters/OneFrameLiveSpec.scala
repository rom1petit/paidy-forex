package forex.services.rates.interpreters

import cats.Id
import cats.effect.unsafe.implicits.global
import forex.domain.Currency
import forex.domain.Rate.Pair
import forex.programs.emulator.OneFrameServiceEmulator
import org.http4s.Uri
import org.scalatest.flatspec.AnyFlatSpec

class OneFrameLiveSpec extends AnyFlatSpec with OneFrameServiceEmulator {

  it should "successfully build OneFrameService request" in {

    val pair = Pair(Currency.USD, Currency.JPY)

    val actual = OneFrameLive
      .getPairRatesRequest[Id](Uri.unsafeFromString(s"http://localhost:8080"), "10dc303535874aeccc86a8251e6992f5", pair)

    actual.uri.toString() shouldBe "http://localhost:8080/rates?pair=USDJPY"
  }

  it should "successfully get USD/JPY rate from OneFrameService" in {

    val pair = Pair(Currency.USD, Currency.JPY)

    serviceResource
      .use(service => service.get(pair))
      .map {
        case Left(value) => fail(s"Unexpected error $value")
        case Right(rate) => rate.pair shouldBe pair
      }
      .unsafeRunSync()
  }
}
