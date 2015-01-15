package security

import models.User
import repository._
import securesocial.core.authenticator.CookieAuthenticator

trait ProfileCookieAuthenticatorStore extends ProfileAuthenticatorStore[CookieAuthenticator[User]] {
  def toAuthenticator(user: User, authenticator: AuthenticatorDetails): CookieAuthenticator[User] =
    CookieAuthenticator(
      authenticator.id,
      user,
      authenticator.expirationDate,
      authenticator.lastUsed,
      authenticator.creationDate,
      this)
}
