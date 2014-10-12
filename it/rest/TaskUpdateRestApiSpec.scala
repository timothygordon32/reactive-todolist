package rest

import java.util.UUID

import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test._

class TaskUpdateRestApiSpec extends PlaySpecification {

  "User task REST API" should {

    "updates a task as done" in new WithApplication {

      val identity = cookies(route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get).get("id").head

      var text = "text-" + UUID.randomUUID
      var created = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> text, "done" -> false))
        .withCookies(identity)).get).as[JsObject]
      var JsString(id) = created \ "id"

      val update = created ++ Json.obj("done" -> true)
      val updateResponse = route(FakeRequest(PUT, s"/tasks/$id")
        .withBody(Json.toJson(update))
        .withCookies(identity)).get
      status(updateResponse) must be equalTo NO_CONTENT

      val updated = route(FakeRequest(GET, s"/tasks/$id")
        .withCookies(identity)).get
      status(updated) must be equalTo OK
      contentType(updated) must beSome.which(_ == "application/json")
      contentAsJson(updated).as[JsObject] must be equalTo Json.obj("id" -> id, "text" -> text, "done" -> true)

      await(route(FakeRequest(DELETE, s"/tasks/$id").withCookies(identity)).get)
    }
  }
}
