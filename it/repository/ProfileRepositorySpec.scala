package repository

import models.User
import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import utils.StartedFakeApplication
import securesocial.core._
import securesocial.core.services.SaveMode

class ProfileRepositorySpec extends PlaySpecification with StartedFakeApplication {

  "Profile repository" should {

    "have an index on email" in new ProfileTestCase {
      val indexes = await(profileRepository.indexes())
      indexes.filter(_.key == Seq("email" -> IndexType.Ascending)) must not be empty
    }

    "not have an index on providerId and userId" in new ProfileTestCase {
      val indexes = await(profileRepository.indexes())
      (indexes.filter(_.key == Seq("userId" -> IndexType.Ascending, "providerId" -> IndexType.Ascending)) must be).empty
    }

    "have an index on userId" in new ProfileTestCase {
      val indexes = await(profileRepository.indexes())
      indexes.filter(_.key == Seq("userId" -> IndexType.Ascending)) must not be empty
    }

    "have an index on authenticatorId" in new ProfileTestCase {
      val indexes = await(profileRepository.indexes())
      indexes.filter(_.key == Seq("authenticator.id" -> IndexType.Ascending)) must not be empty
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
  }
}
