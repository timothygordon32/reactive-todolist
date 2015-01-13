package security

import controllers.{CustomMailTemplates, JsonViewTemplates}
import models.User
import repository.{MongoProfileRepositoryComponent, MongoTokenRepositoryComponent}
import securesocial.core.RuntimeEnvironment
import securesocial.core.authenticator.{CookieAuthenticatorBuilder, HttpHeaderAuthenticatorBuilder}
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.AuthenticatorService

object SecurityEnvironment
  extends RuntimeEnvironment.Default[User]
  with MongoUserServiceComponent
  with MongoTokenRepositoryComponent
  with MongoProfileRepositoryComponent
  with ProfileCookieAuthenticatorStoreComponent
  with ProfileHttpHeaderAuthenticatorStoreComponent {

  lazy val mongoUserService = new MongoUserService
  lazy val tokenRepository = mongoUserService
  lazy val profileRepository = mongoUserService
  lazy val profileCookieAuthenticatorStore = new ProfileCookieAuthenticatorStore
  lazy val profileHttpHeaderAuthenticatorStore = new ProfileHttpHeaderAuthenticatorStore

  override lazy val userService = mongoUserService
  override lazy val authenticatorService = new AuthenticatorService[User](
    new CookieAuthenticatorBuilder[User](profileCookieAuthenticatorStore, idGenerator),
    new HttpHeaderAuthenticatorBuilder[User](profileHttpHeaderAuthenticatorStore, idGenerator)
  )
  override lazy val currentHasher = new PasswordHasher.Default(12)
  override lazy val mailTemplates = CustomMailTemplates
  override lazy val viewTemplates = JsonViewTemplates
}
