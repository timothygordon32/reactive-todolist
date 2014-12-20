package security

import controllers.{JsonViewTemplates, CustomMailTemplates}
import models.User
import securesocial.controllers.{ViewTemplates, MailTemplates}
import securesocial.core.RuntimeEnvironment
import securesocial.core.authenticator.{CookieAuthenticatorBuilder, HttpHeaderAuthenticatorBuilder}
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.{AuthenticatorService, UserService}

object SecurityEnvironment extends RuntimeEnvironment.Default[User] {
  override lazy val userService: UserService[User] = new MongoUserService
  override lazy val authenticatorService: AuthenticatorService[User] = new AuthenticatorService[User](
    new CookieAuthenticatorBuilder[User](new ProfileCookieAuthenticatorStore, idGenerator),
    new HttpHeaderAuthenticatorBuilder[User](new ProfileHttpHeaderAuthenticatorStore, idGenerator)
  )
  override lazy val currentHasher: PasswordHasher = new PasswordHasher.Default(12)
  override lazy val mailTemplates: MailTemplates = CustomMailTemplates
  override lazy val viewTemplates: ViewTemplates = JsonViewTemplates
}
