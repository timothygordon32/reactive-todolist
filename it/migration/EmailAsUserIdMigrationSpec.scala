package migration

import java.util.UUID

import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.test._
import securesocial.core.services.SaveMode
import security.{ApplicationRuntimeEnvironment, MongoUserService}
import utils.StartedFakeApplication
import securesocial.core._

class EmailAsUserIdMigrationSpec extends PlaySpecification with StartedFakeApplication {

  "Old profiles" should {

    "be migrated automatically" in new MigrationTestCase {
      skipped
      // Given
      await(profileRepository.save(profile1, SaveMode.SignUp)).username must be equalTo userId1
      await(profileRepository.save(profile2, SaveMode.SignUp)).username  must be equalTo userId2
      def includeProfile(p: BasicProfile): Boolean = Seq(userId1, userId2).contains(p.userId)
      // When
      await(ApplicationRuntimeEnvironment.migrator.migrateProfileUsernameToEmail(includeProfile))
      // Then
      eventually(await(profileRepository.find(ProviderId, email(userId1))) must not be None)
      eventually(await(profileRepository.find(ProviderId, userId1)) must be equalTo None)
      eventually(await(profileRepository.find(ProviderId, email(userId2))) must not be None)
      eventually(await(profileRepository.find(ProviderId, userId2)) must be equalTo None)
    }

    "be accessible by their email address" in new MigrationTestCase {
      skipped
      await(profileRepository.save(profile1, SaveMode.SignUp)).username must be equalTo userId1

      val oldIdentity = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> userId1, "password" -> password))).get).get("id").get

      val text = "text-" + UUID.randomUUID
      await(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> text, "done" -> false))
        .withCookies(oldIdentity)).get)

      val newIdentityResponse = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> email1, "password" -> password))).get
      val maybeCookie = cookies(newIdentityResponse).get("id")
      maybeCookie must not be None
      val newIdentity = maybeCookie.get

      val tasksForNewIdentity = route(FakeRequest(GET, "/tasks")
        .withCookies(newIdentity)).get
      status(tasksForNewIdentity) must be equalTo OK
      contentAsJson(tasksForNewIdentity).as[JsArray].value.head.as[JsObject] \ "text" must be equalTo JsString(text)
    }
  }
}
