package models

import models.Task._
import org.specs2.mutable._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

class TaskSpec extends Specification {

  "JSON serialization" should {
    "should serialize to JSON" in {
      val id = Some(BSONObjectID.generate)
      val expectedJson = Json.obj(
        "id" -> id.get.stringify,
        "label" -> "I'm new",
        "done" -> false)
      Json.toJson(Task(id, "I'm new")) must be equalTo expectedJson
    }

    "round trip validation and parse of JSON to recreate serialized object" in {
      val task = Task(Some(BSONObjectID.generate), "I'm new")
      Json.toJson(task).validate[Task].get must be equalTo task
    }
  }
}
