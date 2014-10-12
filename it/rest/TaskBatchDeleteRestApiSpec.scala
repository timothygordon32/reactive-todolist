package rest

import java.util.UUID

import play.api.http.HeaderNames
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.test._

class TaskBatchDeleteRestApiSpec extends PlaySpecification {

  "User task REST API" should {

    "delete the completed tasks in JSON" in new WithApplication {
      // Given
      val identity = cookies(route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get).get("id").head
      // And
      var textTodo = "text-" + UUID.randomUUID
      var createdTodo = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> textTodo, "done" -> false))
        .withCookies(identity)).get)
      var JsString(idTodo) = createdTodo \ "id"
      // And
      var textDone = "text-" + UUID.randomUUID
      var createdDone = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> textDone, "done" -> true))
        .withCookies(identity)).get)
      var JsString(idDone) = createdDone \ "id"

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
