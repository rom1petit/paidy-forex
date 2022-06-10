package forex.config

import org.http4s.Uri

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrameClient: OneFrameClientConfig
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class OneFrameClientConfig(
    endpoint: Uri,
    token: String
)
