package models

import play.api.libs.json.Json

case class User(username: String, firstName: Option[String])

object User {
  implicit val writes = Json.writes[User]
}