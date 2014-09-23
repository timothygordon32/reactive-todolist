package repository

import java.util.UUID

import models.Task
import play.api.test._

class TaskRepositoryCRDSpec extends PlaySpecification {

  "Task repository" should {

    "retrieve and delete a saved task" in new WithApplication {
      val label = s"label-${UUID.randomUUID()}"
      val created = await(TaskRepository.create(Task(None, label)))
      val id = created.id.get.stringify

      val found = await(TaskRepository.find(id))
      found.get must be equalTo created

      await(TaskRepository.deleteTask(id)) must be equalTo true
      await(TaskRepository.find(id)) must be equalTo None
      await(TaskRepository.deleteTask(id)) must be equalTo false
    }
  }
}
