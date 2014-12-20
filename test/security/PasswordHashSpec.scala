package security

import org.specs2.mutable._
import securesocial.core.PasswordInfo

class PasswordHashSpec extends Specification {

  import PasswordHash._

  "custom password hasher" should {

    "hash a password consistently" in {
      val hashed = hash("secret1")

      verify("secret1", hashed) must beTrue
    }
  }

  "secure social password hasher" should {

    "match a password info" in {

      val hashed = hash("secret1")

      SecurityEnvironment.currentHasher.matches(
        PasswordInfo(SecurityEnvironment.currentHasher.id, hashed), "secret1") should beTrue
    }
  }
}
