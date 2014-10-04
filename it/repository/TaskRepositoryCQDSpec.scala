package repository

import java.util.UUID

import models.{User, Task}
import play.api.test.{PlaySpecification, WithApplication}
import reactivemongo.bson.BSONObjectID

class TaskRepositoryCQDSpec extends PlaySpecification {

  "User task repository" should {

    "list tasks" in new WithApplication {
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
  }
}
