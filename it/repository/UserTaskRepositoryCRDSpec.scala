package repository

import java.util.UUID

import models.{User, Task}
import play.api.test._

class UserTaskRepositoryCRDSpec extends PlaySpecification {

  "User task repository" should {

    "retrieve and delete a saved task" in new WithApplication {
      implicit val user = User(UUID.randomUUID.toString)
      val label = s"label-${UUID.randomUUID()}"
      val created = await(UserTaskRepository.create(Task(None, label)))
      val id = created.id.get.stringify

      val found = await(UserTaskRepository.find(id))
      found.get must be equalTo created

      await(UserTaskRepository.deleteTask(id)) must be equalTo true
      await(UserTaskRepository.find(id)) must be equalTo None
      await(UserTaskRepository.deleteTask(id)) must be equalTo false
    }
  }
}
