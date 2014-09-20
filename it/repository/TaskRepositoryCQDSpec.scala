package repository

import models.Task
import play.api.test.{PlaySpecification, WithApplication}

class TaskRepositoryCQDSpec extends PlaySpecification {

  "Task repository" should {

    "list tasks" in new WithApplication {
      val created1 = await(TaskRepository.create(Task(None, "label")))
      val created2 = await(TaskRepository.create(Task(None, "label")))

      val list = await(TaskRepository.findAll)

      list must contain(created1)
      list must contain(created2)

      await(TaskRepository.deleteTask(created1.id.get.stringify)) must be equalTo true
      await(TaskRepository.deleteTask(created2.id.get.stringify)) must be equalTo true
    }
  }
}
