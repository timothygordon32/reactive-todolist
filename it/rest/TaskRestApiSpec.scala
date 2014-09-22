package rest

import java.util.UUID

import play.api.libs.json.{JsArray, JsString, Json}
import play.api.test._

class TaskRestApiSpec extends PlaySpecification {

  "Task REST API" should {

    "respond 404 for an unknown URL" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "list the tasks in JSON" in new WithApplication {

      var label = "label-" + UUID.randomUUID
      var created = contentAsJson(route(FakeRequest(POST, "/tasks").withBody(Json.obj("label" -> label))).get)
      var JsString(id) = created \ "id"

      val tasksWithCreated = route(FakeRequest(GET, "/tasks")).get
      status(tasksWithCreated) must equalTo(OK)
      contentType(tasksWithCreated) must beSome.which(_ == "application/json")
      contentAsJson(tasksWithCreated).as[JsArray].value must contain(Json.obj("id" -> id, "label" -> label))

      await(route(FakeRequest(DELETE, s"/tasks/$id")).get)

      val tasksWithoutCreated = route(FakeRequest(GET, "/tasks")).get
      contentAsJson(tasksWithoutCreated).as[JsArray].value must not contain Json.obj("id" -> id, "label" -> label)
    }
  }
}
