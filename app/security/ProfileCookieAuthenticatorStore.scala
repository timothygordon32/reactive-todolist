package security

import models.User
import repository.{ProfileRepository, AuthenticatorDetails, ProfileAuthenticatorStore}
import securesocial.core.authenticator.CookieAuthenticator

class ProfileCookieAuthenticatorStore(override val profiles: ProfileRepository)
  extends ProfileAuthenticatorStore[CookieAuthenticator[User]] {

  def toAuthenticator(user: User, authenticator: AuthenticatorDetails): CookieAuthenticator[User] =
    CookieAuthenticator(
      authenticator.id,
      user,
      authenticator.expirationDate,
      authenticator.lastUsed,
      authenticator.creationDate,
      this)
}
