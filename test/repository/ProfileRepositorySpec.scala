package repository

import org.specs2.mutable._
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import securesocial.core.AuthenticationMethod

class ProfileRepositorySpec extends Specification {

  "JSON serialization" should {
    "read a document in legacy format" in {
      // Given
      implicit val authMethodWrites = Json.writes[AuthenticationMethod]
      val id = BSONObjectID.generate
      val dbDocument = Json.obj(
        "_id" -> Json.obj("$oid" -> id.stringify),
        "providerId" -> "userpass",
        "userId" -> "someone@nomail.com",
        "authMethod" -> Json.toJson(AuthenticationMethod.UserPassword))
      // When
      val task = dbDocument.as[IdentifiedProfile]
      // Then
      task must be equalTo IdentifiedProfile(
        _id = id,
        providerId = "userpass",
        userId = "someone@nomail.com",
        firstName = None,
        lastName = None,
        fullName = None,
        email = None,
        avatarUrl = None,
        authMethod = AuthenticationMethod.UserPassword,
        authenticators = Seq.empty)
    }
  }
}
