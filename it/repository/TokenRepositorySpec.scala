package repository

import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}
import play.api.test.PlaySpecification
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.indexes.IndexType
import securesocial.core.providers.MailToken
import utils.StartedFakeApplication

class TokenRepositorySpec extends PlaySpecification with StartedFakeApplication {

  "Token repository" should {

    "save and find then delete a token" in {
      val repo = new TokenRepository {}
      val now = DateTime.now(DateTimeZone.UTC)
      val expireSoon = now.plusMinutes(1)
      val token = MailToken(UUID.randomUUID.toString, "someone@nomail.com", now, expireSoon, isSignUp = true)

      await(repo.saveToken(token))

      await(repo.findToken(token.uuid)) must be equalTo Some(token)

      await(repo.deleteToken(token.uuid)) must be equalTo Some(token)

      await(repo.findToken(token.uuid)) must be equalTo None
    }

    "save and find then expire a token" in {
      val repo = new TokenRepository {}
      val now = DateTime.now(DateTimeZone.UTC)
      val expired = now.minusMinutes(1)
      val token = MailToken(UUID.randomUUID.toString, "someone@nomail.com", now, expired, isSignUp = true)

      await(repo.saveToken(token))

      await(repo.findToken(token.uuid)) must be equalTo Some(token)

      repo.deleteExpiredTokens()

      eventually {
        await(repo.findToken(token.uuid)) must be equalTo None
      }
    }

    "have an index on uuid" in {
      val repo = new TokenRepository {}
      val indexes = await(repo.indexes())
      indexes.filter(_.key == Seq("uuid" -> IndexType.Ascending)) must not be empty
    }

    "have an index on expiration time" in {
      val repo = new TokenRepository {}
      val indexes = await(repo.indexes())
      indexes.filter(_.key == Seq("expirationTime" -> IndexType.Ascending)) must not be empty
    }
  }
}
