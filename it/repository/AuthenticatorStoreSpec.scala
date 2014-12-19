package repository

import models.User
import play.api.test.PlaySpecification
import securesocial.core.authenticator.{CookieAuthenticator, IdGenerator}
import securesocial.core.services.SaveMode
import time.DateTimeUtils._
import utils.StartedFakeApplication

class AuthenticatorStoreSpec extends PlaySpecification with StartedFakeApplication {

  "Authenticator store" should {

    "find a cookie authenticator" in new ProfileTestCase {
      val store = new ProfileAuthenticatorStore[CookieAuthenticator[User]] {}
      val user = await(store.save(profile, SaveMode.SignUp))
      val created = now
      val expire = created.plusHours(1)
      val lastUsed = created.plusMillis(1)
      val authenticatorId = await(new IdGenerator.Default().generate)
      await(store.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, store), 3600))

      val authenticator = await(store.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo expire
      authenticator.lastUsed must be equalTo lastUsed
      authenticator.creationDate must be equalTo created
    }

    "delete an authenticator" in new ProfileTestCase {
      val store = new ProfileAuthenticatorStore[CookieAuthenticator[User]] {}
      val user = await(store.save(profile, SaveMode.SignUp))
      val created = now
      val expire = created.plusHours(1)
      val lastUsed = created.plusMillis(1)
      val authenticatorId = await(new IdGenerator.Default().generate)
      await(store.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, store), 3600))

      await(store.delete(authenticatorId))

      await(store.find(authenticatorId)) must be equalTo None
    }

    "not find an expired authenticator" in new ProfileTestCase {
      val store = new ProfileAuthenticatorStore[CookieAuthenticator[User]] {}
      val user = await(store.save(profile, SaveMode.SignUp))
      val created = now.minusHours(1)
      val expire = created.plusHours(1)
      val lastUsed = created
      val authenticatorId = await(new IdGenerator.Default().generate)
      await(store.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, store), 3600))

      val authenticator = await(store.find(authenticatorId))

      await(store.find(authenticatorId)) must be equalTo None
    }

    "find a http header authenticator" in pending

    "have an index on authenticator id" in pending

    "be preserved after profile updates" in pending
  }
}




