package security

import models.User
import repository.{AuthenticatorDetails, ProfileAuthenticatorStore}
import securesocial.core.authenticator.HttpHeaderAuthenticator

trait ProfileHttpHeaderAuthenticatorStore extends ProfileAuthenticatorStore[HttpHeaderAuthenticator[User]] {
  def toAuthenticator(user: User, authenticator: AuthenticatorDetails): HttpHeaderAuthenticator[User] =
    HttpHeaderAuthenticator(
      authenticator.id,
      user,
      authenticator.expirationDate,
      authenticator.lastUsed,
      authenticator.creationDate,
      this)
}
