package security

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import repository.ProfileRepository
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode
import securesocial.core.{AuthenticationMethod, BasicProfile, PasswordInfo}
import utils.UniqueStrings

trait UserGeneration extends UniqueStrings with FutureAwaits with DefaultAwaitTimeout {

  def generateUnregisteredUser = {
    val someUniqueString = uniqueString
    User(firstName = s"FirstName-$someUniqueString", userId = s"test-userid-$someUniqueString@nomail.com", password = s"test-password-$someUniqueString")
  }

  def generateRegisteredUser: User = registerUser(generateUnregisteredUser)

  def registerUser(user: User): User = {
    val hash = PasswordHash.hash(user.password)

    val profile = BasicProfile(
      providerId = UsernamePasswordProvider.UsernamePassword,
      userId = user.userId,
      firstName = Some(user.firstName),
      lastName = None,
      fullName = None,
      email = Some(user.userId),
      avatarUrl = None,
      authMethod = AuthenticationMethod.UserPassword,
      oAuth1Info = None,
      oAuth2Info = None,
      passwordInfo = Some(PasswordInfo(hasher = "bcrypt", password = hash.hashed, salt = Some(hash.salt)))
    )

    await(new ProfileRepository {}.save(profile, SaveMode.SignUp))

    user
  }
}
