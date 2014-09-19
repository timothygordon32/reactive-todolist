import org.specs2.mutable._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

import models.Task
import models.Task._

class TaskSpec extends Specification {

  val task = Task(Some(BSONObjectID.generate), "I'm new")

  "JSON serialization" should {
    "round trip validation and parse of JSON to recreate serialized object" in {
      Json.toJson(task).validate[Task].get must be equalTo task
    }

    "should serialize to JSON" in {
      Json.obj("id" -> task.id.get.stringify, "label" -> task.label) must be_==(Json.toJson(task))
    }
  }
}
