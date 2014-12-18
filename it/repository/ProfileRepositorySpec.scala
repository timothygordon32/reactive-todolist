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
      val indexes = await(repo.indexes())
      indexes.filter(_.key == Seq("email" -> IndexType.Ascending)) must not be empty
    }

    "have an index on providerId and userId" in new ProfileTestCase {
      val indexes = await(repo.indexes())
      indexes.filter(_.key == Seq("userId" -> IndexType.Ascending, "providerId" -> IndexType.Ascending)) must not be empty
    }

    "save profile and find a user by provider and email" in new ProfileTestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(repo.findByEmailAndProvider(email, providerId = providerId)) must be equalTo Some(profile)
    }

    "save profile and find a user by provider and user id" in new ProfileTestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(repo.find(providerId, userId)) must be equalTo Some(profile)
    }

    "save profile and find user password info" in new ProfileTestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(repo.passwordInfoFor(User(id, userId, firstName))) must be equalTo profile.passwordInfo
    }

    "update a password for a user" in new ProfileTestCase {
      await(repo.save(profile, SaveMode.SignUp))
      val user = await(repo.findKnown(profile.userId))

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(repo.updatePasswordInfo(user, changedInfo)).get.passwordInfo must be equalTo Some(changedInfo)

      await(repo.passwordInfoFor(user)) must be equalTo Some(changedInfo)
    }

    "sign in the user" in new ProfileTestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(repo.save(profile, SaveMode.LoggedIn)).username must be equalTo userId
    }

    "change the user password" in new ProfileTestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId
      val user = await(repo.findKnown(profile.userId))

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(repo.save(profile.copy(passwordInfo = Some(changedInfo)), SaveMode.PasswordChange)).username must be equalTo userId

      await(repo.passwordInfoFor(user)) must be equalTo Some(changedInfo)
    }

    "link the user to the profile" in new ProfileTestCase {
      val user = User(id, userId, firstName)
      await(repo.link(user, profile)) must be equalTo user
    }
  }
}
