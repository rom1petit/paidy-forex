package forex

import cats.effect._
import cats.implicits._
import forex.Main.{DummyToken, DummyUser}
import forex.config._
import forex.domain.Rate
import forex.http.security.BearerTokenAuth.BearerTokenHandler
import forex.http.security.{BearerTokenAuth, User}
import forex.services.RatesServices
import fs2.Stream
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import tsec.authentication.TSecBearerToken
import tsec.common.SecureRandomId

import java.time.{Instant, Period}
import java.util.UUID
import scala.concurrent.ExecutionContext

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream(ExecutionContext.global).compile.drain.as(ExitCode.Success)

  // Hardcoded User & Token for simplicity

  val DummyUser = User(UUID.fromString("00000000-0000-0000-0000-000000000000"))

  val DummyToken = TSecBearerToken[UUID](
    SecureRandomId.coerce("f294a63479ceac267bad48596e450447217cd2a354d7ba8aa906631f8067d8bc"),
    DummyUser.id,
    Instant.now().plus(Period.ofDays(1)),
    None
  )

}

class Application[F[_]: Async] {

  def buildSecurity(): F[BearerTokenHandler[F]] =
    for {
      userStorage <- Ref.of[F, Map[UUID, User]](Map(DummyUser.id -> DummyUser))
      tokenStorage <- Ref.of[F, Map[SecureRandomId, TSecBearerToken[UUID]]](Map(DummyToken.id -> DummyToken))
    } yield {
      BearerTokenAuth.build[F](userStorage, tokenStorage)
    }

  def buildModule(config: ApplicationConfig): Resource[F, Module[F]] =
    for {
      client <- BlazeClientBuilder[F].resource
      security <- Resource.eval(buildSecurity())
      cache <- Resource.eval(Ref[F].of(Map.empty[Rate.Pair, Rate]))

    } yield {
      val liveRatesService = RatesServices.mixed[F](
        forex.services.time.Clock(),
        config.rateExpiry,
        cache,
        config.oneFrameClient,
        client
      )
      new Module[F](config, security, liveRatesService)
    }

  def stream(ec: ExecutionContext): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module <- Stream.resource(buildModule(config))
      _ <- BlazeServerBuilder[F]
            .withExecutionContext(ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()
}
