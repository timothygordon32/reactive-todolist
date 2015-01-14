package repository

import org.specs2.specification.Scope
import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import securesocial.core.providers.MailToken
import time.DateTimeUtils._
import utils.{StartedFakeApplication, UniqueStrings}

class TokenRepositorySpec extends PlaySpecification with StartedFakeApplication with UniqueStrings {

  "Token repository" should {

    "save and find then delete a token" in new MongoTokenRepositoryComponent with Scope {
      val tokenRepository = new MongoTokenRepository {}
      val created = now
      val expireSoon = created.plusMinutes(1)
      val token = MailToken(uniqueString, "someone@nomail.com", created, expireSoon, isSignUp = true)

      await(tokenRepository.saveToken(token))

      await(tokenRepository.findToken(token.uuid)) must be equalTo Some(token)

      await(tokenRepository.deleteToken(token.uuid)) must be equalTo Some(token)

      await(tokenRepository.findToken(token.uuid)) must be equalTo None
    }

    "save and find then expire a token" in new MongoTokenRepositoryComponent with Scope {
      val tokenRepository = new MongoTokenRepository {}
      val created = now.minusMinutes(2)
      val expired = now.minusMinutes(1)
      val token = MailToken(uniqueString, "someone@nomail.com", created, expired, isSignUp = true)

      await(tokenRepository.saveToken(token))

      await(tokenRepository.findToken(token.uuid)) must be equalTo Some(token)

      tokenRepository.deleteExpiredTokens()

      eventually {
        await(tokenRepository.findToken(token.uuid)) must be equalTo None
      }
    }

    "have an index on uuid" in new MongoTokenRepositoryComponent with Scope {
      val tokenRepository = new MongoTokenRepository {}
      val indexes = await(tokenRepository.indexes())
      indexes.filter(_.key == Seq("uuid" -> IndexType.Ascending)) must not be empty
    }

    "have an index on expiration time" in new MongoTokenRepositoryComponent with Scope {
      val tokenRepository = new MongoTokenRepository {}
      val indexes = await(tokenRepository.indexes())
      indexes.filter(_.key == Seq("expirationTime" -> IndexType.Ascending)) must not be empty
    }
  }
}
