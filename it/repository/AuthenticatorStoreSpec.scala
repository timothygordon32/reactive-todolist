package repository

import models.User
import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import securesocial.core.authenticator.{CookieAuthenticator, IdGenerator}
import securesocial.core.services.SaveMode
import time.DateTimeUtils._
import utils.StartedFakeApplication

class AuthenticatorStoreSpec extends PlaySpecification with StartedFakeApplication {

  "Authenticator store" should {

    "find a cookie authenticator" in new AuthenticatorTestCase {
      val user = await(store.save(profile, SaveMode.SignUp))
      val created = now
      val expire = created.plusHours(1)
      val lastUsed = created.plusMillis(1)
      await(store.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, store), 3600))

      val authenticator = await(store.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo expire
      authenticator.lastUsed must be equalTo lastUsed
      authenticator.creationDate must be equalTo created
    }

    "delete a cookie authenticator" in new AuthenticatorTestCase {
      val user = await(store.save(profile, SaveMode.SignUp))
      val created = now
      val expire = created.plusHours(1)
      val lastUsed = created.plusMillis(1)
      await(store.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, store), 3600))

      await(store.delete(authenticatorId))

      await(store.find(authenticatorId)) must be equalTo None
    }

    "not find an expired cookie authenticator" in new AuthenticatorTestCase {
      val user = await(store.save(profile, SaveMode.SignUp))
      val created = now.minusHours(1)
      val expire = created.plusHours(1)
      val lastUsed = created
      await(store.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, store), 3600))

      val authenticator = await(store.find(authenticatorId))

      await(store.find(authenticatorId)) must be equalTo None
    }

    "have an index on authenticator id" in new AuthenticatorTestCase {
      val indexes = await(store.indexes())
      indexes.filter(_.key == Seq("authenticator.id" -> IndexType.Ascending)) must not be empty
    }

    "find a http header authenticator" in pending
  }

  trait AuthenticatorTestCase extends ProfileTestCase {
    val authenticatorId = await(new IdGenerator.Default().generate)

    val store = new ProfileAuthenticatorStore[CookieAuthenticator[User]] {}
  }
}
