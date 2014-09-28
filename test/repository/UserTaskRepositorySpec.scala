package repository

import models.Task
import org.specs2.mutable._
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

class UserTaskRepositorySpec extends Specification {

  "JSON serialization" should {
    "read a document in legacy format" in {
      // Given
      import TaskRepositoryFormats.taskReads
      val id = BSONObjectID.generate.stringify
      val dbDocument = Json.obj(
         "_id" -> Json.obj("$oid" -> id),
         "label" -> "I'm new",
         "done" -> false)
      // When
      val task = dbDocument.as[Task]
      // Then
      task must be equalTo Task(Some(BSONObjectID(id)), "I'm new", done = false)
    }

    "write back a document in current format" in {
      // Given
      import TaskRepositoryFormats.taskWrites
      val id = BSONObjectID.generate.stringify
      val task = Task(Some(BSONObjectID(id)), "I'm new", done = false)
      // When
      val serialized = Json.toJson(task)
      // Then
      serialized must be equalTo Json.obj(
        "_id" -> Json.obj("$oid" -> id),
        "text" -> "I'm new",
        "done" -> false)
    }
  }
}
