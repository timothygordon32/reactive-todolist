package rest

import java.util.UUID

import play.api.libs.json.{JsArray, JsString, Json}
import play.api.mvc.Security
import play.api.test._
import reactivemongo.bson.BSONObjectID

class TaskRestApiSpec extends PlaySpecification {

  "User task REST API" should {

    "list the tasks in JSON" in new WithApplication {

      val session = Security.username -> "test"
      var label = "label-" + UUID.randomUUID
      var created = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("label" -> label))
        .withSession(session)).get)
      var JsString(id) = created \ "id"

      val tasksWithCreated = route(FakeRequest(GET, "/tasks")
        .withSession(session)).get
      status(tasksWithCreated) must be equalTo OK
      contentType(tasksWithCreated) must beSome.which(_ == "application/json")
      contentAsJson(tasksWithCreated).as[JsArray].value must contain(Json.obj("id" -> id, "label" -> label))

      await(route(FakeRequest(DELETE, s"/tasks/$id").withSession(session)).get)

      val tasksWithoutCreated = route(FakeRequest(GET, "/tasks").withSession(session)).get
      contentAsJson(tasksWithoutCreated).as[JsArray].value must not contain Json.obj("id" -> id, "label" -> label)
    }
  }
}
