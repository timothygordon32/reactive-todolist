package models

import play.api.libs.json.Json

case class User(username: String)

object User {
  implicit val writes = Json.writes[User]
}