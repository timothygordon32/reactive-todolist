package repository

import java.util.UUID

import models.{Task, User}
import play.api.test.{PlaySpecification, WithApplication}
import reactivemongo.bson.BSONObjectID

class TaskRepositoryBatchDeleteSpec extends PlaySpecification {

  "User task repository" should {

    "batch delete tasks" in new WithApplication {
      // Given
      implicit val user = User(UUID.randomUUID.toString)
      val doneTask = Task(Some(BSONObjectID.generate), "done", done = true)
      val notDoneTask = Task(Some(BSONObjectID.generate), "not-done", done = false)
      // And
      val createdDoneTask = await(TaskRepository.create(doneTask))
      val createdNotDoneTask = await(TaskRepository.create(notDoneTask))
      // And
      val created = await(TaskRepository.findAll)
      created must contain(createdDoneTask, createdNotDoneTask)

      // When
      await(TaskRepository.deleteDone)

      // Then
      val remaining = await(TaskRepository.findAll)
      remaining must contain(createdNotDoneTask).atMostOnce
      remaining must not contain createdDoneTask

      // Cleanup
      await(TaskRepository.delete(createdNotDoneTask.id.get.stringify)) must be equalTo true
    }
  }
}
