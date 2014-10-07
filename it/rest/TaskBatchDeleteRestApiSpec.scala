package rest

import java.util.UUID

import play.api.http.HeaderNames
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.mvc.{Result, Security}
import play.api.test._

import scala.concurrent.Future

class TaskBatchDeleteRestApiSpec extends PlaySpecification {

  "User task REST API" should {

    "delete the completed tasks in JSON" in new WithApplication {
      // Given
      val session = Security.username -> "test"
      // And
      var textTodo = "text-" + UUID.randomUUID
      var createdTodo = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> textTodo, "done" -> false))
        .withSession(session)).get)
      var JsString(idTodo) = createdTodo \ "id"
      // And
      var textDone = "text-" + UUID.randomUUID
      var createdDone = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> textDone, "done" -> true))
        .withSession(session)).get)
      var JsString(idDone) = createdDone \ "id"

      // When
      val batchDeleteResponse = route(FakeRequest(DELETE, s"/tasks/done").withSession(session)).get

      // Then
      status(batchDeleteResponse) must be equalTo SEE_OTHER
      header(HeaderNames.LOCATION, batchDeleteResponse) must be equalTo Some("/tasks")
      // And
      val tasksJsonArray = contentAsJson(route(FakeRequest(GET, s"/tasks").withSession(session)).get).as[JsArray].value
      tasksJsonArray must contain (Json.obj("id" -> idTodo, "text" -> textTodo, "done" -> false))
      tasksJsonArray must not contain Json.obj("id" -> idDone, "text" -> textDone, "done" -> true)
    }
  }
}
