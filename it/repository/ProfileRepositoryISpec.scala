package repository

import models.User
import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import time.DateTimeUtils._
import utils.StartedFakeApplication
import securesocial.core._
import securesocial.core.services.SaveMode

class ProfileRepositoryISpec extends PlaySpecification with StartedFakeApplication {

  "Profile repository" should {

    "have an index on email" in new ProfileTestCase {
      val indexes = await(profileRepository.indexes())
      indexes.filter(_.key == Seq("email" -> IndexType.Ascending)) must not be empty
    }

    "have an index on userId" in new ProfileTestCase {
      val indexes = await(profileRepository.indexes())
      indexes.filter(_.key == Seq("userId" -> IndexType.Ascending)) must not be empty
    }

    "not have an index on authenticatorId" in new ProfileTestCase {
      val indexes = await(profileRepository.indexes())
      (indexes.filter(_.key == Seq("authenticator.id" -> IndexType.Ascending)) must be).empty
    }

    "have an index on authenticator ids" in new ProfileTestCase {
      val indexes = await(profileRepository.indexes())
      indexes.filter(_.key == Seq("authenticators.id" -> IndexType.Ascending)) must not be empty
    }

    "save profile and find a user by provider and email" in new ProfileTestCase {
      await(profileRepository.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(profileRepository.findByEmailAndProvider(email, providerId = providerId)) must be equalTo Some(profile)
    }

    "save profile and find a user by provider and user id" in new ProfileTestCase {
      await(profileRepository.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(profileRepository.find(providerId, userId)) must be equalTo Some(profile)
    }

    "save profile and find user password info" in new ProfileTestCase {
      await(profileRepository.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(profileRepository.passwordInfoFor(User(id, userId, firstName))) must be equalTo profile.passwordInfo
    }

    "update a password for a user" in new ProfileTestCase {
      await(profileRepository.save(profile, SaveMode.SignUp))
      val user = await(profileRepository.findKnown(profile.userId))

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(profileRepository.updatePasswordInfo(user, changedInfo)).get.passwordInfo must be equalTo Some(changedInfo)

      await(profileRepository.passwordInfoFor(user)) must be equalTo Some(changedInfo)
    }

    "sign in the user" in new ProfileTestCase {
      await(profileRepository.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(profileRepository.save(profile, SaveMode.LoggedIn)).username must be equalTo userId
    }

    "change the user password" in new ProfileTestCase {
      await(profileRepository.save(profile, SaveMode.SignUp)).username must be equalTo userId
      val user = await(profileRepository.findKnown(profile.userId))

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(profileRepository.save(profile.copy(passwordInfo = Some(changedInfo)), SaveMode.PasswordChange)).username must be equalTo userId

      await(profileRepository.passwordInfoFor(user)) must be equalTo Some(changedInfo)
    }

    "link the user to the profile" in new ProfileTestCase {
      val user = User(id, userId, firstName)
      await(profileRepository.link(user, profile)) must be equalTo user
    }

    "be able to store multiple authentication details per user" in new ProfileTestCase {
      await(profileRepository.save(profile, SaveMode.SignUp)).username must be equalTo userId
      val user = await(profileRepository.findKnown(profile.userId))

      val justCreated = now
      val notExpired = justCreated.plusHours(1)
      val justUsed = justCreated
      val authenticatorDetails = AuthenticatorDetails(authenticatorId, notExpired, justUsed, justCreated)
      val anotherAuthenticatorId = generateAuthenticatorId
      val anotherAuthenticatorDetails = authenticatorDetails.copy(id = anotherAuthenticatorId)
      await(profileRepository.saveAuthenticator(user, authenticatorDetails))
      await(profileRepository.saveAuthenticator(user, anotherAuthenticatorDetails))

      val userAuthenticatorDetails = await(profileRepository.findAuthenticator(authenticatorId)).get
      val anotherUserAuthenticatorDetails = await(profileRepository.findAuthenticator(anotherAuthenticatorId)).get

      userAuthenticatorDetails.user must be equalTo user
      userAuthenticatorDetails.authenticatorDetails.expirationDate must be equalTo notExpired
      userAuthenticatorDetails.authenticatorDetails.lastUsed must be equalTo justUsed
      userAuthenticatorDetails.authenticatorDetails.creationDate must be equalTo justCreated
      anotherUserAuthenticatorDetails.user must be equalTo user
      anotherUserAuthenticatorDetails.authenticatorDetails.expirationDate must be equalTo notExpired
      anotherUserAuthenticatorDetails.authenticatorDetails.lastUsed must be equalTo justUsed
      anotherUserAuthenticatorDetails.authenticatorDetails.creationDate must be equalTo justCreated
    }
  }
}
