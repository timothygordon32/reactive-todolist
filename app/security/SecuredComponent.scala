package security

import models.User
import securesocial.core.RuntimeEnvironment

trait SecuredComponent {
  implicit val env: RuntimeEnvironment[User] = SecurityEnvironment
}
