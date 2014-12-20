package security

import models.User
import repository.ProfileAuthenticatorStore
import securesocial.core.authenticator.CookieAuthenticator

class ProfileCookieAuthenticatorStore extends ProfileAuthenticatorStore[CookieAuthenticator[User]] {
  def toAuthenticator(userWithAuthenticator: UserWithAuthenticator): CookieAuthenticator[User] =
    CookieAuthenticator(
      userWithAuthenticator.authenticator.id,
      userWithAuthenticator.toUser,
      userWithAuthenticator.authenticator.expirationDate,
      userWithAuthenticator.authenticator.lastUsed,
      userWithAuthenticator.authenticator.creationDate,
      this)
}
