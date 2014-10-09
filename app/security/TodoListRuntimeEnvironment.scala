package security

import models.User
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.UserService

object TodoListRuntimeEnvironment extends RuntimeEnvironment.Default[User] {
  override lazy val userService: UserService[User] = PlayConfigurationUserService
  override lazy val currentHasher: PasswordHasher = new PasswordHasher.Default(12)
}
