package models

import play.api.libs.json._
import reactivemongo.bson._

case class Task(id: Option[BSONObjectID], label: String, done: Boolean = false)

object Task {
  implicit val taskFormat = {
    import BSONFormats.BSONObjectIDFormat
    Json.format[Task]
  }
}