package security

import controllers.PasswordHash
import org.specs2.mutable._



class PasswordHashSpec extends Specification {

  import controllers.PasswordHash._

    "password hasher" should {

      "hash a password consistently" in {
        val hashed = hash("secret")

        verify("secret", hashed) must be equalTo true
      }
    }
}
