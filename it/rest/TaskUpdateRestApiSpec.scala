package rest

import java.util.UUID

import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.Security
import play.api.test._

class TaskUpdateRestApiSpec extends PlaySpecification {

  "User task REST API" should {

    "updates a task as done" in new WithApplication {

      val session = Security.username -> "test"
      var text = "text-" + UUID.randomUUID
      var created = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> text, "done" -> false))
        .withSession(session)).get).as[JsObject]
      var JsString(id) = created \ "id"

      val update = created ++ Json.obj("done" -> true)
      val updateResponse = route(FakeRequest(PUT, s"/tasks/$id")
        .withBody(Json.toJson(update))
        .withSession(session)).get
      status(updateResponse) must be equalTo NO_CONTENT

      val updated = route(FakeRequest(GET, s"/tasks/$id")
        .withSession(session)).get
      status(updated) must be equalTo OK
      contentType(updated) must beSome.which(_ == "application/json")
      contentAsJson(updated).as[JsObject] must be equalTo Json.obj("id" -> id, "text" -> text, "done" -> true)

      await(route(FakeRequest(DELETE, s"/tasks/$id").withSession(session)).get)
    }
  }
}
