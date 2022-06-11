package forex.http.security

import cats.effect.{Ref, Sync}
import tsec.authentication._
import tsec.common.SecureRandomId

import java.util.UUID
import scala.concurrent.duration._

object BearerTokenAuth {

  type BearerTokenHandler[F[_]] = SecuredRequestHandler[F, UUID, User, TSecBearerToken[UUID]]

  def build[F[_]: Sync](
      userStorage: Ref[F, Map[UUID, User]],
      tokenStorage: Ref[F, Map[SecureRandomId, TSecBearerToken[UUID]]]
  ): BearerTokenHandler[F] = {

    val bearerTokenStore: BackingStore[F, SecureRandomId, TSecBearerToken[UUID]] =
      InMemoryBackingStore[F, SecureRandomId, TSecBearerToken[UUID]](tokenStorage, s => SecureRandomId.coerce(s.id))

    val userStore: BackingStore[F, UUID, User] =
      InMemoryBackingStore[F, UUID, User](userStorage, _.id)

    val settings: TSecTokenSettings = TSecTokenSettings(
      expiryDuration = 100.minutes,
      maxIdle = None
    )

    val bearerTokenAuth: BearerTokenAuthenticator[F, UUID, User] =
      BearerTokenAuthenticator(
        bearerTokenStore,
        userStore,
        settings
      )

    SecuredRequestHandler(bearerTokenAuth)
  }
}
