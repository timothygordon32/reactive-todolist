package repository

import java.util.UUID

import models.{Task, User}
import play.api.test.{PlaySpecification, WithApplication}

class TaskRepositoryUpdateSpec extends PlaySpecification {

  "User task repository" should {

    "update a task" in new WithApplication {
      // Given
      implicit val user = User(UUID.randomUUID.toString)
      val label = s"label-${UUID.randomUUID()}"
      val created = await(TaskRepository.create(Task(None, label)))
      created.done must beFalse

      // When
      val update = created.copy(done = true)
      await(TaskRepository.update(update))

      // Then
      val id = created.id.get.stringify
      val reRead = await(TaskRepository.find(id))
      reRead.get.done must beTrue

      // Cleanup
      await(TaskRepository.delete(id)) must be equalTo true
    }
  }
}
