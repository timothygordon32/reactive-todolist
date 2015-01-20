package repository

import play.api.test.PlaySpecification
import securesocial.core.authenticator.{CookieAuthenticator, HttpHeaderAuthenticator}
import securesocial.core.services.SaveMode
import security.{ProfileCookieAuthenticatorStore, ProfileHttpHeaderAuthenticatorStore}
import time.DateTimeUtils._
import utils.StartedFakeApplication

class AuthenticatorStoreISpec extends PlaySpecification with StartedFakeApplication {

  "Authenticator store" should {

    "find a valid unexpired cookie authenticator" in new AuthenticatorTestCase {
      val user = await(profileRepository.save(profile, SaveMode.SignUp))
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
      val user = await(profileRepository.save(profile, SaveMode.SignUp))
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(profileCookieAuthenticatorStore.save(CookieAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, profileCookieAuthenticatorStore), 3600))

      await(profileCookieAuthenticatorStore.delete(authenticatorId))

      await(profileCookieAuthenticatorStore.find(authenticatorId)) must be equalTo None
    }

    "expired cookie authenticators should be evicted on save" in new AuthenticatorTestCase {
      val user = await(profileRepository.save(profile, SaveMode.SignUp))
      val anHourOld = now.minusHours(1)
      val justExpired = now.minusMillis(1)
      val usedAtCreation = anHourOld
      await(profileCookieAuthenticatorStore.save(CookieAuthenticator(authenticatorId, user, justExpired, usedAtCreation, anHourOld, profileCookieAuthenticatorStore), 3600))

      val authenticator = await(profileCookieAuthenticatorStore.find(authenticatorId))

      authenticator must be equalTo None
    }

    "find a valid unexpired http header authenticator" in new AuthenticatorTestCase {
      val user = await(profileRepository.save(profile, SaveMode.SignUp))
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
      val user = await(profileRepository.save(profile, SaveMode.SignUp))
      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      await(profileHttpHeaderAuthenticatorStore.save(HttpHeaderAuthenticator(authenticatorId, user, notExpired, justUsed, justCreated, profileHttpHeaderAuthenticatorStore), 3600))

      await(profileHttpHeaderAuthenticatorStore.delete(authenticatorId))

      await(profileHttpHeaderAuthenticatorStore.find(authenticatorId)) must be equalTo None
    }

    "expired HTTP authenticators should be evicted on save" in new AuthenticatorTestCase {
      val user = await(profileRepository.save(profile, SaveMode.SignUp))
      val anHourOld = now.minusHours(1)
      val justExpired = now.minusMillis(1)
      val usedAtCreation = anHourOld
      await(profileHttpHeaderAuthenticatorStore.save(HttpHeaderAuthenticator(authenticatorId, user, justExpired, usedAtCreation, anHourOld, profileHttpHeaderAuthenticatorStore), 3600))

      val authenticator = await(profileHttpHeaderAuthenticatorStore.find(authenticatorId))

      authenticator must be equalTo None
    }
  }

  trait AuthenticatorTestCase extends ProfileTestCase {
    val profileCookieAuthenticatorStore: ProfileCookieAuthenticatorStore = new ProfileCookieAuthenticatorStore {
      def profiles: ProfileRepository = profileRepository
    }
    val profileHttpHeaderAuthenticatorStore: ProfileHttpHeaderAuthenticatorStore = new ProfileHttpHeaderAuthenticatorStore {
      def profiles: ProfileRepository = profileRepository
    }
  }
}
