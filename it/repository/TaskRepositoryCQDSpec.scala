package repository

import java.util.UUID

import models.{User, Task}
import play.api.test.{PlaySpecification, WithApplication}

class TaskRepositoryCQDSpec extends PlaySpecification {

  "User task repository" should {

    "list tasks" in new WithApplication {
      // Given
      implicit val user = User(UUID.randomUUID.toString)
      // And
      val created1 = await(TaskRepository.create(Task(None, "text")))
      val created2 = await(TaskRepository.create(Task(None, "text")))

      // When
      val list = await(TaskRepository.findAll)

      // Then
      list must contain(created1)
      list must contain(created2)

      // Cleanup
      await(TaskRepository.delete(created1.id.get.stringify)) must be equalTo true
      await(TaskRepository.delete(created2.id.get.stringify)) must be equalTo true
    }
  }
}
