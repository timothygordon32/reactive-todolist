package security

import controllers.{JsonViewTemplates, CustomMailTemplates}
import models.User
import repository.MongoTokenRepositoryComponent
import securesocial.controllers.{ViewTemplates, MailTemplates}
import securesocial.core.RuntimeEnvironment
import securesocial.core.authenticator.{CookieAuthenticatorBuilder, HttpHeaderAuthenticatorBuilder}
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.{AuthenticatorService, UserService}

object SecurityEnvironment
  extends RuntimeEnvironment.Default[User]
  with MongoSecureSocialUserServiceComponent
  with MongoTokenRepositoryComponent {

  lazy val mongoUserService = new MongoUserService
  lazy val tokenRepository = mongoUserService

  override lazy val userService: UserService[User] = mongoUserService
  override lazy val authenticatorService: AuthenticatorService[User] = new AuthenticatorService[User](
    new CookieAuthenticatorBuilder[User](new ProfileCookieAuthenticatorStore(mongoUserService), idGenerator),
    new HttpHeaderAuthenticatorBuilder[User](new ProfileHttpHeaderAuthenticatorStore(mongoUserService), idGenerator)
  )
  override lazy val currentHasher: PasswordHasher = new PasswordHasher.Default(12)
  override lazy val mailTemplates: MailTemplates = CustomMailTemplates
  override lazy val viewTemplates: ViewTemplates = JsonViewTemplates
}
