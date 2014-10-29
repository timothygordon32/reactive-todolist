package repository

import java.util.UUID

import models.{User, Task}
import play.api.test.{PlaySpecification, WithApplication}
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import utils.StartedFakeApplication

class TaskRepositorySpec extends PlaySpecification with StartedFakeApplication {

  "User task repository" should {

    "list tasks" in {
      // Given
      implicit val user = User(UUID.randomUUID.toString)
      val first = Task(Some(BSONObjectID.generate), "first")
      val second = Task(Some(BSONObjectID.generate), "second")
      // And
      val created2 = await(TaskRepository.create(second))
      val created1 = await(TaskRepository.create(first))

      // When
      val list = await(TaskRepository.findAll)

      // Then
      list must contain(created1, created2).inOrder.atMost

      // Cleanup
      await(TaskRepository.delete(created1.id.get.stringify)) must be equalTo true
      await(TaskRepository.delete(created2.id.get.stringify)) must be equalTo true
    }

    "retrieve and delete a saved task" in {
      // Given
      implicit val user = User(UUID.randomUUID.toString)
      val text = s"text-${UUID.randomUUID()}"

      // When
      val created = await(TaskRepository.create(Task(None, text)))

      // Then
      val id = created.id.get.stringify
      val found = await(TaskRepository.find(id))
      found.get must be equalTo created

      // Cleanup
      await(TaskRepository.delete(id)) must be equalTo true
      await(TaskRepository.find(id)) must be equalTo None
      await(TaskRepository.delete(id)) must be equalTo false
    }

    "update a task" in {
      // Given
      implicit val user = User(UUID.randomUUID.toString)
      val text = s"text-${UUID.randomUUID()}"
      val created = await(TaskRepository.create(Task(None, text)))
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

    "batch delete tasks" in {
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

    "have an index on user" in {
      // Given
      val repo = TaskRepository
      // When
      val indexes = await(TaskRepository.indexes())
      // Then
      indexes.filter(_.key == Seq("user" -> IndexType.Ascending)) must not be empty
    }
  }
}
