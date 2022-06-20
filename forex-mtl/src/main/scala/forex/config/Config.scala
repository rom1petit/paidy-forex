package forex.config

import cats.effect.{ Resource, Sync }
import org.http4s.Uri
import pureconfig.generic.auto._
import pureconfig.{ ConfigReader, ConfigSource }

object Config {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader[String].map(Uri.unsafeFromString)

  /**
    * @param path the property path inside the default configuration
    */
  def resource[F[_]: Sync](path: String): Resource[F, ApplicationConfig] =
    Resource.eval(Sync[F].delay(ConfigSource.default.at(path).loadOrThrow[ApplicationConfig]))

}
