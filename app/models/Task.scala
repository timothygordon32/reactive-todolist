package models

import play.api.libs.json._
import reactivemongo.bson._

case class Task(id: Option[BSONObjectID], label: String)

object Task {
  import BSONFormats.BSONObjectIDFormat

  implicit val taskFormat = Json.format[Task]
}