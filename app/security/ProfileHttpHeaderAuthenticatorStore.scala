package security

import models.User
import repository.ProfileAuthenticatorStore
import securesocial.core.authenticator.HttpHeaderAuthenticator

class ProfileHttpHeaderAuthenticatorStore extends ProfileAuthenticatorStore[HttpHeaderAuthenticator[User]] {
  def toAuthenticator(userWithAuthenticator: UserWithAuthenticator): HttpHeaderAuthenticator[User] =
    HttpHeaderAuthenticator(
      userWithAuthenticator.authenticator.id,
      userWithAuthenticator.toUser,
      userWithAuthenticator.authenticator.expirationDate,
      userWithAuthenticator.authenticator.lastUsed,
      userWithAuthenticator.authenticator.creationDate,
      this)
}
