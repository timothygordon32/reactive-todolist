package models

import play.api.libs.json._
import reactivemongo.bson._

case class Task(id: Option[BSONObjectID] = None, text: String, done: Boolean = false)

object Task {
  implicit val taskFormat = {
    implicit val bsonObjectIdFormat = BSONFormats.BSONObjectIDFormat
    Json.format[Task]
  }
}