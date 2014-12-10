package rest

import java.util.UUID

import org.specs2.execute.Pending
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsArray, JsString, Json}
import play.api.test._
import utils.StartedFakeApplication

class TaskRestApiSpec extends PlaySpecification with StartedFakeApplication {

  "User task REST API" should {

    "list the tasks in JSON" in {

      val identity = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> "testuser1@nomail.com", "password" -> "!secret1"))).get).get("id").head

      val text = "text-" + UUID.randomUUID
      val created = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> text, "done" -> false))
        .withCookies(identity)).get)
      val JsString(id) = created \ "id"

      val tasksWithCreated = route(FakeRequest(GET, "/tasks")
        .withCookies(identity)).get
      status(tasksWithCreated) must be equalTo OK
      contentType(tasksWithCreated) must beSome.which(_ == "application/json")
      contentAsJson(tasksWithCreated).as[JsArray].value must contain(Json.obj("id" -> id, "text" -> text, "done" -> false))

      await(route(FakeRequest(DELETE, s"/tasks/$id").withCookies(identity)).get)

      val tasksWithoutCreated = route(FakeRequest(GET, "/tasks").withCookies(identity)).get
      contentAsJson(tasksWithoutCreated).as[JsArray].value must not contain Json.obj("id" -> id, "text" -> text, "done" -> false)
    }

    "updates a task as done" in {

      val identity = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> "testuser1@nomail.com", "password" -> "!secret1"))).get).get("id").head

      val text = "text-" + UUID.randomUUID
      val created = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> text, "done" -> false))
        .withCookies(identity)).get).as[JsObject]
      val JsString(id) = created \ "id"

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

      status(route(FakeRequest(DELETE, s"/tasks/$id").withCookies(identity)).get) must be equalTo NO_CONTENT
    }

    "delete all completed tasks for a user" in eventually {
      // Given
      val identity = cookies(route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser2@nomail.com", "password" -> "!secret2"))).get).get("id").head
      // And
      val textTodo = "text-" + UUID.randomUUID
      val createdTodo = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> textTodo, "done" -> false))
        .withCookies(identity)).get)
      val JsString(idTodo) = createdTodo \ "id"
      // And
      val textDone = "text-" + UUID.randomUUID
      val createdDone = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> textDone, "done" -> true))
        .withCookies(identity)).get)
      val JsString(idDone) = createdDone \ "id"

      // When
      val batchDeleteResponse = route(FakeRequest(DELETE, s"/tasks/done").withCookies(identity)).get

      // Then
      status(batchDeleteResponse) must be equalTo SEE_OTHER
      header(HeaderNames.LOCATION, batchDeleteResponse) must be equalTo Some("/tasks")
      // And
      val tasksJsonArray = contentAsJson(route(FakeRequest(GET, s"/tasks").withCookies(identity)).get).as[JsArray].value
      tasksJsonArray must contain (Json.obj("id" -> idTodo, "text" -> textTodo, "done" -> false))
      tasksJsonArray must not contain Json.obj("id" -> idDone, "text" -> textDone, "done" -> true)
    }
  }
}
