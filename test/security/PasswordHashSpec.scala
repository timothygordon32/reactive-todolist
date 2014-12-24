package security

import org.specs2.mutable._
import securesocial.core.PasswordInfo

class PasswordHashSpec extends Specification {

  "custom password hasher" should {

    "hash a password consistently" in {
      val hashed = PasswordHash.hash("secret1").hashed

      PasswordHash.verify("secret1", hashed) must beTrue
    }
  }

  "secure social password hasher" should {

    "match a password info" in {

      val hashed = PasswordHash.hash("secret1")

      SecurityEnvironment.currentHasher.matches(
        PasswordInfo(SecurityEnvironment.currentHasher.id, hashed.hashed, Some(hashed.salt)), "secret1") should beTrue
    }
  }
}
