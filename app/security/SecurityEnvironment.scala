package security

import controllers.{CustomMailTemplates, JsonViewTemplates}
import models.User
import repository.ProfileRepository
import securesocial.core.RuntimeEnvironment
import securesocial.core.authenticator.{CookieAuthenticatorBuilder, HttpHeaderAuthenticatorBuilder}
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.AuthenticatorService

object SecurityEnvironment extends RuntimeEnvironment.Default[User] {

  lazy val mongoUserService = MongoUserService
  lazy val tokenRepository = mongoUserService
  lazy val profileRepository = mongoUserService
  lazy val profileCookieAuthenticatorStore = new ProfileCookieAuthenticatorStore {
    def profiles: ProfileRepository = profileRepository
  }
  lazy val profileHttpHeaderAuthenticatorStore = new ProfileHttpHeaderAuthenticatorStore {
    def profiles: ProfileRepository = profileRepository
  }

  override lazy val userService = mongoUserService
  override lazy val authenticatorService = new AuthenticatorService[User](
    new CookieAuthenticatorBuilder[User](profileCookieAuthenticatorStore, idGenerator),
    new HttpHeaderAuthenticatorBuilder[User](profileHttpHeaderAuthenticatorStore, idGenerator)
  )
  override lazy val currentHasher = new PasswordHasher.Default(12)
  override lazy val mailTemplates = CustomMailTemplates
  override lazy val viewTemplates = JsonViewTemplates
}
