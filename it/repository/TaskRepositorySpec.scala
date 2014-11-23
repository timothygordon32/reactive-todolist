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

    "change ownership from userId to user id" in {
      // Given
      val repo = TaskRepository
      implicit val user = randomUser
      val task1 = await(repo.create(Task(text = randomString), user.username))
      val task2 = await(repo.create(Task(text = randomString), user.username))
      // When
      await(repo.migrateUserId(user))
      // Then
      val tasks = await(repo.findAll(user.id))
      tasks must contain(task1, task2).inOrder.atMost
    }
  }
}
