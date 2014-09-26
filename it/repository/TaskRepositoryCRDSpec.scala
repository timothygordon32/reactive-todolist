package repository

import java.util.UUID

import models.{User, Task}
import play.api.test._

class TaskRepositoryCRDSpec extends PlaySpecification {

  "User task repository" should {

    "retrieve and delete a saved task" in new WithApplication {
      // Given
      implicit val user = User(UUID.randomUUID.toString)
      val label = s"label-${UUID.randomUUID()}"

      // When
      val created = await(TaskRepository.create(Task(None, label)))

      // Then
      val id = created.id.get.stringify
      val found = await(TaskRepository.find(id))
      found.get must be equalTo created

      // Cleanup
      await(TaskRepository.delete(id)) must be equalTo true
      await(TaskRepository.find(id)) must be equalTo None
      await(TaskRepository.delete(id)) must be equalTo false
    }
  }
}
