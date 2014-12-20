package security

import controllers.{JsonViewTemplates, CustomMailTemplates}
import models.User
import securesocial.controllers.{ViewTemplates, MailTemplates}
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.UserService

object SecurityEnvironment extends RuntimeEnvironment.Default[User] {
  override lazy val userService: UserService[User] = new MongoUserService
  override lazy val currentHasher: PasswordHasher = new PasswordHasher.Default(12)
  override lazy val mailTemplates: MailTemplates = CustomMailTemplates
  override lazy val viewTemplates: ViewTemplates = JsonViewTemplates
}
