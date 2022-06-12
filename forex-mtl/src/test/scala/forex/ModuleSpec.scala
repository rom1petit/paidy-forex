package forex

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import forex.Main.DummyToken
import forex.config.{ApplicationConfig, HttpConfig, OneFrameClientConfig}
import forex.domain.{Currency, Rate}
import forex.http.rates.Protocol._
import forex.services.rates.interpreters.OneFrameDummy
import forex.services.rates.interpreters.OneFrameDummy.{DummyPrice, DummyTime}
import forex.services.rates.{Algebra, errors}
import org.http4s.Method.GET
import org.http4s.headers.Authorization
import org.http4s.{Header, Headers, Request, Status, Uri}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.concurrent.duration.DurationInt

class ModuleSpec extends AnyFlatSpec {

  it should "return Ok with rates when valid request" in new Scope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=USD&to=JPY"))
        .withHeaders(Headers(Header.Raw(Authorization.name, "Bearer " + DummyToken.id)))

    (for {
      actual <- module.httpApp.run(request)
      response <- actual.as[GetApiResponse]
    } yield {
      actual.status shouldBe Status.Ok
      response shouldBe GetApiResponse(Currency.USD, Currency.JPY, DummyPrice, DummyTime)
    }).unsafeRunSync()
  }

  it should "return NotFound when rate lookup fail" in new FailingLookupScope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=USD&to=JPY"))
        .withHeaders(Headers(Header.Raw(Authorization.name, "Bearer " + DummyToken.id)))

    val actual = module.httpApp.run(request).unsafeRunSync()
    actual.status shouldBe Status.NotFound
  }

  it should "return NotFound when missing parameter" in new Scope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=BTC"))
        .withHeaders(Headers(Header.Raw(Authorization.name, "Bearer " + DummyToken.id)))

    val actual = module.httpApp.run(request).unsafeRunSync()
    actual.status shouldBe Status.NotFound
  }

  it should "return BadRequest when currency is unknown" in new Scope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=BTC&to=ADA"))
        .withHeaders(Headers(Header.Raw(Authorization.name, "Bearer " + DummyToken.id)))

    val actual = module.httpApp.run(request).unsafeRunSync()
    actual.status shouldBe Status.BadRequest
  }

  it should "return Unauthorized when token is missing" in new Scope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=USD&to=JPY"))

    val actual = module.httpApp.run(request).unsafeRunSync()
    actual.status shouldBe Status.Unauthorized
  }

  it should "return Unauthorized when token is unknown" in new Scope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=USD&to=JPY"))
        .withHeaders(Headers(Header.Raw(Authorization.name, "Bearer " + "XYZ")))

    val actual = module.httpApp.run(request).unsafeRunSync()
    actual.status shouldBe Status.Unauthorized
  }

  it should "return InternalError when call fails" in new InternalErrorScope {

    val request =
      Request[IO](method = GET, uri = Uri.unsafeFromString("/rates?from=USD&to=JPY"))
        .withHeaders(Headers(Header.Raw(Authorization.name, "Bearer " + DummyToken.id)))

    val actual = module.httpApp.run(request).unsafeRunSync()
    actual.status shouldBe Status.InternalServerError
  }

  trait Scope {
    val config =
      ApplicationConfig(
        HttpConfig("####", 0, 1.second),
        OneFrameClientConfig(Uri.unsafeFromString("/"), ""),
        5.minutes
      )

    def module =
      new Module[IO](config, new Application[IO].buildSecurity().unsafeRunSync(), new OneFrameDummy())
  }

  trait FailingLookupScope extends Scope {

    val failing = new Algebra[IO] {
      override def get(pair: Rate.Pair): IO[Either[errors.Error, Rate]] =
        IO.pure(Left(errors.Error.OneFrameLookupFailed(s"missing ${pair}")))
    }

    override def module =
      new Module[IO](config, new Application[IO].buildSecurity().unsafeRunSync(), failing)
  }

  trait InternalErrorScope extends Scope {

    val failing = new Algebra[IO] {
      override def get(pair: Rate.Pair): IO[Either[errors.Error, Rate]] =
        IO.raiseError(new Exception("BTOOOM !"))
    }

    override def module =
      new Module[IO](config, new Application[IO].buildSecurity().unsafeRunSync(), failing)
  }
}
