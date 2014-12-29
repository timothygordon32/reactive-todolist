package rest

import java.util.UUID

import play.api.http.HeaderNames
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.test._
import security.{User, Users}
import utils.StartedFakeApplication

class TaskRestApiSpec extends PlaySpecification with StartedFakeApplication with Users {

  def logon(user: User) =
    cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
      .withBody(Json.obj("username" -> user.userId, "password" -> user.password))).get).get("id").head

  lazy val user1Cookie = logon(User1)

  lazy val user2Cookie = logon(User2)

  "User task REST API" should {

    "list the tasks in JSON" in {

      val identity = user1Cookie
      val text = "text-" + UUID.randomUUID
      val created = contentAsJson(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> text, "done" -> false))
        .withCookies(identity)).get)
      val JsString(id) = created \ "id"

      val tasksWithCreated = route(FakeRequest(GET, "/tasks")
        .withCookies(identity)).get
      status(tasksWithCreated) must be equalTo OK
      contentType(tasksWithCreated) must beSome.which(_ == "application/json")
      println(contentAsJson(tasksWithCreated))
      contentAsJson(tasksWithCreated).as[JsArray].value must contain(Json.obj("id" -> id, "text" -> text, "done" -> false))

      await(route(FakeRequest(DELETE, s"/tasks/$id").withCookies(identity)).get)

      val tasksWithoutCreated = route(FakeRequest(GET, "/tasks").withCookies(identity)).get
      contentAsJson(tasksWithoutCreated).as[JsArray].value must not contain Json.obj("id" -> id, "text" -> text, "done" -> false)
    }

  }
}
