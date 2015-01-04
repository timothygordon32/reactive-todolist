package repository

import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import securesocial.core.authenticator.{CookieAuthenticator, HttpHeaderAuthenticator, IdGenerator}
import securesocial.core.services.SaveMode
import security.{ProfileCookieAuthenticatorStore, ProfileHttpHeaderAuthenticatorStore}
import time.DateTimeUtils._
import utils.StartedFakeApplication

class AuthenticatorStoreSpec extends PlaySpecification with StartedFakeApplication {

  "Authenticator store" should {

    "find a valid unexpired cookie authenticator" in new AuthenticatorTestCase {
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(cookieStore.save(CookieAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, cookieStore), 3600))

      val authenticator = await(cookieStore.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo notExpired
      authenticator.lastUsed must be equalTo justUsed
      authenticator.creationDate must be equalTo justCreated
    }

    "delete a cookie authenticator" in new AuthenticatorTestCase {
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(cookieStore.save(CookieAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, cookieStore), 3600))

      await(cookieStore.delete(authenticatorId))

      await(cookieStore.find(authenticatorId)) must be equalTo None
    }

    "not find an expired cookie authenticator" in new AuthenticatorTestCase {
      val anHourOld = now.minusHours(1)
      val justExpired = now.minusMillis(1)
      val usedAtCreation = anHourOld
      await(cookieStore.save(CookieAuthenticator(authenticatorId, user, justExpired, usedAtCreation, anHourOld, cookieStore), 3600))

      val authenticator = await(cookieStore.find(authenticatorId))

      await(cookieStore.find(authenticatorId)) must be equalTo None
    }

    "find a valid unexpired http header authenticator" in new AuthenticatorTestCase {
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(httpHeaderStore.save(HttpHeaderAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, httpHeaderStore), 3600))

      val authenticator = await(httpHeaderStore.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo notExpired
      authenticator.lastUsed must be equalTo justUsed
      authenticator.creationDate must be equalTo justCreated
    }

    "delete a HTTP authenticator" in new AuthenticatorTestCase {
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(httpHeaderStore.save(HttpHeaderAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, httpHeaderStore), 3600))

      await(httpHeaderStore.delete(authenticatorId))

      await(httpHeaderStore.find(authenticatorId)) must be equalTo None
    }

    "not find an expired HTTP authenticator" in new AuthenticatorTestCase {
      val anHourOld = now.minusHours(1)
      val justExpired = now.minusMillis(1)
      val usedAtCreation = anHourOld
      await(httpHeaderStore.save(HttpHeaderAuthenticator(authenticatorId, user, justExpired, usedAtCreation, anHourOld, httpHeaderStore), 3600))

      val authenticator = await(httpHeaderStore.find(authenticatorId))

      await(httpHeaderStore.find(authenticatorId)) must be equalTo None
    }
  }

  trait AuthenticatorTestCase extends ProfileTestCase {
    val authenticatorId = await(new IdGenerator.Default().generate)
    val cookieStore = new ProfileCookieAuthenticatorStore(repo)
    val httpHeaderStore = new ProfileHttpHeaderAuthenticatorStore(repo)
    val user = await(repo.save(profile, SaveMode.SignUp))
  }
}
