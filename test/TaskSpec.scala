import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

import models.Task
import models.Task._

@RunWith(classOf[JUnitRunner])
class TaskSpec extends Specification {

  val task = Task(Some(BSONObjectID.generate), "I'm new")
  val taskjs = Json.toJson(task)

  "JSON serialization" should {
    "round trip validation and parse of JSON to recreate serialized object" in {
      taskjs.validate[Task].get must be_==(task)
    }

    "should serialize to JSON" in {
      Json.obj("id" -> task.id.get.stringify, "label" -> task.label) must be_==(taskjs)
    }
  }
}
