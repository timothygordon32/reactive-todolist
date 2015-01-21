package security

import models.User
import securesocial.core.RuntimeEnvironment

trait SecuredComponent {
  implicit lazy val env: RuntimeEnvironment[User] = SecurityEnvironment
}
