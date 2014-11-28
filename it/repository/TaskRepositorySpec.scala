package repository

import java.util.UUID

import models.{Task, User}
import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import utils.StartedFakeApplication

class TaskRepositorySpec extends PlaySpecification with StartedFakeApplication {

  def randomString = UUID.randomUUID().toString

  def randomUser = User(BSONObjectID.generate, randomString, None)

  "User task repository" should {

    "list tasks" in {
      // Given
      implicit val user = randomUser
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
      implicit val user = randomUser
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
      implicit val user = randomUser
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
      implicit val user = randomUser
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
      val indexes = await(repo.indexes())
      // Then
      indexes.filter(_.key == Seq("user" -> IndexType.Ascending)) must not be empty
    }

    "copy all tasks to another user" in {
      skipped
      // Given
      val repo = TaskRepository
      val oldUser = randomUser
      val task1 = Task(None, randomString)
      await(repo.create(task1)(oldUser))
      val task2 = Task(None, randomString)
      await(repo.create(task2)(oldUser))
      val newUser = randomUser
      // When
      await(repo.copy(fromUserId = oldUser.username, toUserId = newUser.username))
      // Then
      val copied = await(repo.findAll(newUser))
      copied must have size 2
      copied(0).text must be equalTo task1.text
      copied(1).text must be equalTo task2.text
    }

    "load tasks by user id or userId" in {
      skipped
      // Given
      val repo = TaskRepository
      implicit val user = randomUser
      val task1 = await(repo.create(Task(text = randomString), user.username))
      val task2 = await(repo.create(Task(text = randomString), user.id))
      // When
      val tasks = await(repo.findAll)
      // Then
      tasks must contain(task1, task2).inOrder.atMost
    }

    "change ownership from userId to user id" in {
      skipped
      // Given
      val repo = TaskRepository
      implicit val user = randomUser
      val task1 = await(repo.create(Task(text = randomString), user.username))
      val task2 = await(repo.create(Task(text = randomString), user.username))
      // When
      await(repo.migrateTaskOwnershipToUserObjectId(user))
      // Then
      val tasks = await(repo.findAll(user.id))
      tasks must contain(task1, task2).inOrder.atMost
    }
  }
}
