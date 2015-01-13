package repository

import play.api.test.PlaySpecification
import securesocial.core.authenticator.{CookieAuthenticator, HttpHeaderAuthenticator, IdGenerator}
import securesocial.core.services.SaveMode
import security.{ProfileCookieAuthenticatorStoreComponent, ProfileHttpHeaderAuthenticatorStoreComponent}
import time.DateTimeUtils._
import utils.StartedFakeApplication

class AuthenticatorStoreSpec extends PlaySpecification with StartedFakeApplication {

  "Authenticator store" should {

    "find a valid unexpired cookie authenticator" in new AuthenticatorTestCase {
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(profileCookieAuthenticatorStore.save(CookieAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, profileCookieAuthenticatorStore), 3600))

      val authenticator = await(profileCookieAuthenticatorStore.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo notExpired
      authenticator.lastUsed must be equalTo justUsed
      authenticator.creationDate must be equalTo justCreated
    }

    "delete a cookie authenticator" in new AuthenticatorTestCase {
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(profileCookieAuthenticatorStore.save(CookieAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, profileCookieAuthenticatorStore), 3600))

      await(profileCookieAuthenticatorStore.delete(authenticatorId))

      await(profileCookieAuthenticatorStore.find(authenticatorId)) must be equalTo None
    }

    "not find an expired cookie authenticator" in new AuthenticatorTestCase {
      val anHourOld = now.minusHours(1)
      val justExpired = now.minusMillis(1)
      val usedAtCreation = anHourOld
      await(profileCookieAuthenticatorStore.save(CookieAuthenticator(authenticatorId, user, justExpired, usedAtCreation, anHourOld, profileCookieAuthenticatorStore), 3600))

      val authenticator = await(profileCookieAuthenticatorStore.find(authenticatorId))

      await(profileCookieAuthenticatorStore.find(authenticatorId)) must be equalTo None
    }

    "find a valid unexpired http header authenticator" in new AuthenticatorTestCase {
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(profileHttpHeaderAuthenticatorStore.save(HttpHeaderAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, profileHttpHeaderAuthenticatorStore), 3600))

      val authenticator = await(profileHttpHeaderAuthenticatorStore.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo notExpired
      authenticator.lastUsed must be equalTo justUsed
      authenticator.creationDate must be equalTo justCreated
    }

    "delete a HTTP authenticator" in new AuthenticatorTestCase {
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(profileHttpHeaderAuthenticatorStore.save(HttpHeaderAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, profileHttpHeaderAuthenticatorStore), 3600))

      await(profileHttpHeaderAuthenticatorStore.delete(authenticatorId))

      await(profileHttpHeaderAuthenticatorStore.find(authenticatorId)) must be equalTo None
    }

    "not find an expired HTTP authenticator" in new AuthenticatorTestCase {
      val anHourOld = now.minusHours(1)
      val justExpired = now.minusMillis(1)
      val usedAtCreation = anHourOld
      await(profileHttpHeaderAuthenticatorStore.save(HttpHeaderAuthenticator(authenticatorId, user, justExpired, usedAtCreation, anHourOld, profileHttpHeaderAuthenticatorStore), 3600))

      val authenticator = await(profileHttpHeaderAuthenticatorStore.find(authenticatorId))

      await(profileHttpHeaderAuthenticatorStore.find(authenticatorId)) must be equalTo None
    }
  }

  trait AuthenticatorTestCase
    extends ProfileTestCase
    with ProfileCookieAuthenticatorStoreComponent
    with ProfileHttpHeaderAuthenticatorStoreComponent
  {
    val authenticatorId = await(new IdGenerator.Default().generate)

    val profileCookieAuthenticatorStore: ProfileCookieAuthenticatorStore = new ProfileCookieAuthenticatorStore
    val profileHttpHeaderAuthenticatorStore: ProfileHttpHeaderAuthenticatorStore = new ProfileHttpHeaderAuthenticatorStore
    val user = await(profileRepository.save(profile, SaveMode.SignUp))
  }
}
