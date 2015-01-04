package security

import models.User
import repository.{AuthenticatorDetails, ProfileRepository, ProfileAuthenticatorStore}
import securesocial.core.authenticator.HttpHeaderAuthenticator

class ProfileHttpHeaderAuthenticatorStore(override val profiles: ProfileRepository)
  extends ProfileAuthenticatorStore[HttpHeaderAuthenticator[User]] {

  def toAuthenticator(user: User, authenticator: AuthenticatorDetails): HttpHeaderAuthenticator[User] =
    HttpHeaderAuthenticator(
      authenticator.id,
      user,
      authenticator.expirationDate,
      authenticator.lastUsed,
      authenticator.creationDate,
      this)
}
