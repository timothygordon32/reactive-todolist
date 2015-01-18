package repository

import java.util.UUID

import models.{Task, User}
import org.specs2.specification.Scope
import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import utils.StartedFakeApplication

class TaskRepositoryISpec extends PlaySpecification with StartedFakeApplication {

  def randomString = UUID.randomUUID().toString

  def randomUser = User(BSONObjectID.generate, randomString, None)

  "User task repository" should {

    "list tasks" in new TaskRepositoryTestCase {
      // Given
      implicit val user = randomUser
      val first = Task(Some(BSONObjectID.generate), "first")
      val second = Task(Some(BSONObjectID.generate), "second")
      // And
      val created2 = await(taskRepository.create(second))
      val created1 = await(taskRepository.create(first))

      // When
      val list = await(taskRepository.findAll)

      // Then
      list must contain(created1, created2).inOrder.atMost

      // Cleanup
      await(taskRepository.delete(created1.id.get.stringify)) must be equalTo true
      await(taskRepository.delete(created2.id.get.stringify)) must be equalTo true
    }

    "retrieve and delete a saved task" in new TaskRepositoryTestCase {
      // Given
      implicit val user = randomUser
      val text = s"text-${UUID.randomUUID()}"

      // When
      val created = await(taskRepository.create(Task(None, text)))

      // Then
      val id = created.id.get.stringify
      val found = await(taskRepository.find(id))
      found.get must be equalTo created

      // Cleanup
      await(taskRepository.delete(id)) must be equalTo true
      await(taskRepository.find(id)) must be equalTo None
      await(taskRepository.delete(id)) must be equalTo false
    }

    "update a task" in new TaskRepositoryTestCase {
      // Given
      implicit val user = randomUser
      val text = s"text-${UUID.randomUUID()}"
      val created = await(taskRepository.create(Task(None, text)))
      created.done must beFalse

      // When
      val update = created.copy(done = true)
      await(taskRepository.update(update))

      // Then
      val id = created.id.get.stringify
      val reRead = await(taskRepository.find(id))
      reRead.get.done must beTrue

      // Cleanup
      await(taskRepository.delete(id)) must be equalTo true
    }

    "batch delete tasks" in new TaskRepositoryTestCase {
      // Given
      implicit val user = randomUser
      val doneTask = Task(Some(BSONObjectID.generate), "done", done = true)
      val notDoneTask = Task(Some(BSONObjectID.generate), "not-done", done = false)
      // And
      val createdDoneTask = await(taskRepository.create(doneTask))
      val createdNotDoneTask = await(taskRepository.create(notDoneTask))
      // And
      val created = await(taskRepository.findAll)
      created must contain(createdDoneTask, createdNotDoneTask)

      // When
      await(taskRepository.deleteDone)

      // Then
      val remaining = await(taskRepository.findAll)
      remaining must contain(createdNotDoneTask).atMostOnce
      remaining must not contain createdDoneTask

      // Cleanup
      await(taskRepository.delete(createdNotDoneTask.id.get.stringify)) must be equalTo true
    }

    "have an index on user" in new TaskRepositoryTestCase {
      // Given
      val repo = taskRepository
      // When
      val indexes = await(repo.indexes())
      // Then
      indexes.filter(_.key == Seq("user" -> IndexType.Ascending)) must not be empty
    }
  }

  trait TaskRepositoryTestCase extends Scope {
    val taskRepository = MongoTaskRepository
  }
}
