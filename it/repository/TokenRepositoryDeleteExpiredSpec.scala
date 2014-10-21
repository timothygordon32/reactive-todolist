package repository

import java.util.UUID

import org.joda.time.{DateTimeZone, DateTime}
import play.api.test.{PlaySpecification, WithApplication}
import play.modules.reactivemongo.ReactiveMongoPlugin
import securesocial.core.providers.MailToken

class TokenRepositoryDeleteExpiredSpec extends PlaySpecification {

  "Token repository" should {

    "save and find a token" in new WithApplication {
      val repo = new TokenRepository {
        override def db = ReactiveMongoPlugin.db
      }
      val now = DateTime.now(DateTimeZone.UTC)
      val expired = now.minusMinutes(1)
      val token = MailToken(UUID.randomUUID.toString, "someone@nomail.com", now, expired, isSignUp = true)

      await(repo.saveToken(token))

      await(repo.findToken(token.uuid)) must be equalTo Some(token)

      await(repo.deleteToken(token.uuid)) must be equalTo Some(token)

      eventually {
        await(repo.findToken(token.uuid)) must be equalTo None
      }
    }
  }
}
