package repository

import java.util.UUID

import models.{Task, User}
import play.api.test.{PlaySpecification, WithApplication}

class TaskRepositoryUpdateSpec extends PlaySpecification {

  "User task repository" should {

    "list tasks" in new WithApplication {
      implicit val user = User(UUID.randomUUID.toString)
      val label = s"label-${UUID.randomUUID()}"
      val created = await(TaskRepository.create(Task(None, label)))
      created.done must beFalse

      val id = created.id.get.stringify

      val update = created.copy(done = true)
      await(TaskRepository.update(update))

      val reRead = await(TaskRepository.find(id))
      reRead.get.done must beTrue

      await(TaskRepository.delete(id)) must be equalTo true
    }
  }
}
