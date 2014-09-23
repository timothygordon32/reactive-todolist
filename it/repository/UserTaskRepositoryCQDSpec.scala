package repository

import java.util.UUID

import models.{User, Task}
import play.api.test.{PlaySpecification, WithApplication}

class UserTaskRepositoryCQDSpec extends PlaySpecification {

  "User task repository" should {

    "list tasks" in new WithApplication {
      implicit val user = User(UUID.randomUUID.toString)
      val created1 = await(UserTaskRepository.create(Task(None, "label")))
      val created2 = await(UserTaskRepository.create(Task(None, "label")))

      val list = await(UserTaskRepository.findAll)

      list must contain(created1)
      list must contain(created2)

      await(UserTaskRepository.deleteTask(created1.id.get.stringify)) must be equalTo true
      await(UserTaskRepository.deleteTask(created2.id.get.stringify)) must be equalTo true
    }
  }
}
