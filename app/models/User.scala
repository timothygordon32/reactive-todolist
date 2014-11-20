package models

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

case class User(id: BSONObjectID, username: String, firstName: Option[String])

object User {
  implicit val writes: Writes[User] = new Writes[User] {
    override def writes(user: User): JsValue = Json.obj("username" -> user.username, "firstName" -> user.firstName)
  }
}