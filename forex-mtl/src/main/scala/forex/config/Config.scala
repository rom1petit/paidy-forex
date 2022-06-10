package forex.config

import cats.effect.Sync
import fs2.Stream
import org.http4s.Uri
import pureconfig.generic.auto._
import pureconfig.{ConfigReader, ConfigSource}

object Config {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader[String].map(Uri.unsafeFromString)

  /**
    * @param path the property path inside the default configuration
    */
  def stream[F[_]: Sync](path: String): Stream[F, ApplicationConfig] =
    Stream.eval(Sync[F].delay(ConfigSource.default.at(path).loadOrThrow[ApplicationConfig]))

}
