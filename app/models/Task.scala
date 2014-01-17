package models

import reactivemongo.bson._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.modules.reactivemongo.json.BSONFormats._
import models.BSONFormats.BSONObjectIDFormat

case class Task(id: Option[BSONObjectID], label: String)

object Task {

  implicit val taskWrites: Writes[Task] = (
    (__ \ "id").write[Option[BSONObjectID]] and
      (__ \ "label").write[String]
    )(unlift(Task.unapply))

  implicit val taskReads: Reads[Task] = (
    (__ \ "id").read[Option[BSONObjectID]] and
      (__ \ "label").read[String]
    )(Task.apply _)

  implicit object TaskBSONReader extends BSONDocumentReader[Task] {
    def read(doc: BSONDocument): Task =
      Task(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("label").get)
  }

  implicit object TaskBSONWriter extends BSONDocumentWriter[Task] {
    def write(task: Task): BSONDocument =
      BSONDocument(
        "_id" -> task.id.getOrElse(BSONObjectID.generate),
        "label" -> task.label)
  }

}