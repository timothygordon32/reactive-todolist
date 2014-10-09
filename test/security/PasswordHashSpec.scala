package security

import org.specs2.mutable._
import securesocial.core.PasswordInfo

class PasswordHashSpec extends Specification {

  import PasswordHash._

  "custom password hasher" should {

    "hash a password consistently" in {
      val hashed = hash("secret")

      verify("secret", hashed) must beTrue
    }
  }

  "secure social password hasher" should {

    "match a password info" in {

      val hashed = hash("secret")

      TodoListRuntimeEnvironment.currentHasher.matches(
        PasswordInfo(TodoListRuntimeEnvironment.currentHasher.id, hashed), "secret") should beTrue
    }
  }
}
