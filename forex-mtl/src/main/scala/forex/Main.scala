package forex

import cats.effect._
import forex.config._
import forex.http.HttpErrorHandler
import forex.services.RatesServices
import fs2.Stream
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream(ExecutionContext.global).compile.drain.as(ExitCode.Success)

}

class Application[F[_]: Async] {

  def stream(ec: ExecutionContext): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      client <- BlazeClientBuilder[F].stream
      ratesService = RatesServices.live[F](config.oneFrameClient, client)
      module       = new Module[F](config, ratesService)
      _ <- BlazeServerBuilder[F]
            .withExecutionContext(ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .withServiceErrorHandler(new HttpErrorHandler[F]())
            .serve
    } yield ()

}
