package forex

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import forex.config.{ ApplicationConfig, HttpConfig, OneFrameClientConfig }
import forex.domain.Currency
import forex.http.rates.Protocol._
import forex.programs.rates.errors.Error.IllegalArgument
import forex.services.rates.interpreters.OneFrameDummy
import org.http4s.Method.GET
import org.http4s.{ Request, Status, Uri }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.{ an, convertToAnyShouldWrapper }

import scala.concurrent.duration.DurationInt

class ModuleSpec extends AnyFlatSpec {

  it should "successfully handle valid GET rates request" in new Scope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=USD&to=JPY"))

    (for {
      actual <- module.httpApp.run(request)
      response <- actual.as[GetApiResponse]
    } yield {
      actual.status shouldBe Status.Ok
      response shouldBe GetApiResponse(Currency.USD, Currency.JPY, OneFrameDummy.DummyPrice, response.timestamp)
    }).unsafeRunSync()
  }

  it should "fail to handle invalid GET rates request" in new Scope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=USD&to=ADA"))

    an[IllegalArgument] should be thrownBy
      module.httpApp.run(request).unsafeRunSync()
  }

  trait Scope {
    val config =
      ApplicationConfig(HttpConfig("####", 0, 1.second), OneFrameClientConfig(Uri.unsafeFromString("/"), ""))

    val module = new Module[IO](config, new OneFrameDummy())
  }
}
