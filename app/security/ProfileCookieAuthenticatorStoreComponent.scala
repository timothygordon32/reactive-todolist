package security

import models.User
import repository._
import securesocial.core.authenticator.CookieAuthenticator

trait ProfileCookieAuthenticatorStoreComponent extends ProfileAuthenticatorStoreComponent {

  val profileCookieAuthenticatorStore: ProfileCookieAuthenticatorStore

  class ProfileCookieAuthenticatorStore extends ProfileAuthenticatorStore[CookieAuthenticator[User]] {
    def toAuthenticator(user: User, authenticator: AuthenticatorDetails): CookieAuthenticator[User] =
      CookieAuthenticator(
        authenticator.id,
        user,
        authenticator.expirationDate,
        authenticator.lastUsed,
        authenticator.creationDate,
        this)
  }
}
