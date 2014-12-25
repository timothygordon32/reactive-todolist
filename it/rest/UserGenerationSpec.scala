package rest

import play.api.libs.json._
import play.api.test.{FakeRequest, PlaySpecification}
import repository.ProfileRepository
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode
import securesocial.core.{AuthenticationMethod, BasicProfile, PasswordInfo}
import security.PasswordHash
import ui.model.User
import utils.{StartedFakeApplication, UniqueStrings}

class UserGenerationSpec extends PlaySpecification with StartedFakeApplication with UserGeneration {

  "Security" should {

    "create a user on demand" in {

      val user = generateRegisteredUser

      val login = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> user.userId, "password" -> user.password))).get

      val id = cookies(login).get("id").head
      val userDescription = route(FakeRequest(GET, "/login").withCookies(id)).get
      status(userDescription) must equalTo(OK)
      contentAsJson(userDescription) \ "username" must be equalTo JsString(user.userId)
    }
  }
}

trait UserGeneration extends UniqueStrings {

  def generateUnregisteredUser = {
    val someUniqueString = uniqueString
    User(firstName = s"FirstName-$someUniqueString", userId = s"test-userid-$someUniqueString@nomail.com", password = s"test-password-$someUniqueString")
  }

  def generateRegisteredUser: User = registerUser(generateUnregisteredUser)

  def registerUser(user: User) = {
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

    new ProfileRepository {}.save(profile, SaveMode.SignUp)

    user
  }
}