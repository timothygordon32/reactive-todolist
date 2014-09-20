package repository


import java.util.UUID

import models.Task
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class TaskRepositorySpec extends PlaySpecification {

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
