package repository

import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}
import play.api.test.{PlaySpecification, WithApplication}
import play.modules.reactivemongo.ReactiveMongoPlugin
import securesocial.core.providers.MailToken

class TokenRepositoryCRDSpec extends PlaySpecification {

  "Token repository" should {

    "save and find a token" in new WithApplication {
      val repo = new TokenRepository {
        override def db = ReactiveMongoPlugin.db
      }
      val now = DateTime.now(DateTimeZone.UTC)
      val expireSoon = now.plusMinutes(1)
      val token = MailToken(UUID.randomUUID.toString, "someone@nomail.com", now, expireSoon, isSignUp = true)

      await(repo.saveToken(token))

      await(repo.findToken(token.uuid)) must be equalTo Some(token)

      await(repo.deleteToken(token.uuid)) must be equalTo Some(token)

      await(repo.findToken(token.uuid)) must be equalTo None
    }
  }
}
