package rest

import java.util.UUID

import play.api.libs.json._
import play.api.test.{FakeRequest, PlaySpecification}
import repository.ProfileRepository
import securesocial.core.{PasswordInfo, AuthenticationMethod, BasicProfile}
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode
import security.PasswordHash
import ui.model.User
import utils.StartedFakeApplication

class UserGenerationSpec extends PlaySpecification with StartedFakeApplication {

  "Security" should {

    "create a user on demand" in {

      def generateUser: User = {
        val someUniqueString = UUID.randomUUID.toString
        val userId = s"$someUniqueString@nomail.com"
        val password = someUniqueString
        val hash = PasswordHash.hash(someUniqueString)
        val firstName = s"I-am-$someUniqueString"
        val generatedUser = User(firstName, userId, password)

        val profile = BasicProfile(
          providerId = UsernamePasswordProvider.UsernamePassword,
          userId = userId,
          firstName = Some(generatedUser.firstName),
          lastName = None,
          fullName = None,
          email = Some(generatedUser.userId),
          avatarUrl = None,
          authMethod = AuthenticationMethod.UserPassword,
          oAuth1Info = None,
          oAuth2Info = None,
          passwordInfo = Some(PasswordInfo(hasher = "bcrypt", password = hash.hashed, salt = Some(hash.salt)))
        )

        new ProfileRepository {}.save(profile, SaveMode.SignUp)

        generatedUser
      }
      val user = generateUser

      val login = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> user.userId, "password" -> user.password))).get

      val id = cookies(login).get("id").head
      val userDescription = route(FakeRequest(GET, "/login").withCookies(id)).get
      status(userDescription) must equalTo(OK)
      contentAsJson(userDescription) \ "username" must be equalTo JsString(user.userId)
    }
  }
}
