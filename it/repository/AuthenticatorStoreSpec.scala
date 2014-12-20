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

    "find a cookie authenticator" in new AuthenticatorTestCase {
      val user = await(cookieStore.save(profile, SaveMode.SignUp))
      val created = now
      val expire = created.plusHours(1)
      val lastUsed = created.plusMillis(1)
      await(cookieStore.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, cookieStore), 3600))

      val authenticator = await(cookieStore.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo expire
      authenticator.lastUsed must be equalTo lastUsed
      authenticator.creationDate must be equalTo created
    }

    "delete a cookie authenticator" in new AuthenticatorTestCase {
      val user = await(cookieStore.save(profile, SaveMode.SignUp))
      val created = now
      val expire = created.plusHours(1)
      val lastUsed = created.plusMillis(1)
      await(cookieStore.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, cookieStore), 3600))

      await(cookieStore.delete(authenticatorId))

      await(cookieStore.find(authenticatorId)) must be equalTo None
    }

    "not find an expired cookie authenticator" in new AuthenticatorTestCase {
      val user = await(cookieStore.save(profile, SaveMode.SignUp))
      val created = now.minusHours(1)
      val expire = created.plusHours(1)
      val lastUsed = created
      await(cookieStore.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, cookieStore), 3600))

      val authenticator = await(cookieStore.find(authenticatorId))

      await(cookieStore.find(authenticatorId)) must be equalTo None
    }

    "have an index on authenticator id" in new AuthenticatorTestCase {
      val indexes = await(cookieStore.indexes())
      indexes.filter(_.key == Seq("authenticator.id" -> IndexType.Ascending)) must not be empty
    }

    "find a http header authenticator" in new AuthenticatorTestCase {
      val user = await(httpHeaderStore.save(profile, SaveMode.SignUp))
      val created = now
      val expire = created.plusHours(1)
      val lastUsed = created.plusMillis(1)
      await(httpHeaderStore.save(HttpHeaderAuthenticator(authenticatorId, user, expire, lastUsed, created, httpHeaderStore), 3600))

      val authenticator = await(httpHeaderStore.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo expire
      authenticator.lastUsed must be equalTo lastUsed
      authenticator.creationDate must be equalTo created
    }
  }

  trait AuthenticatorTestCase extends ProfileTestCase {
    val authenticatorId = await(new IdGenerator.Default().generate)
    val cookieStore = new ProfileCookieAuthenticatorStore
    val httpHeaderStore = new ProfileHttpHeaderAuthenticatorStore
  }
}
