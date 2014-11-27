package migration

import java.util.UUID

import models.Task
import play.api.test.PlaySpecification
import repository.TaskRepository
import securesocial.core.BasicProfile
import securesocial.core.services.SaveMode
import security.MongoUserService
import utils.StartedFakeApplication

class UserTasksOwnedByObjectIdMigrationSpec extends PlaySpecification with StartedFakeApplication {

  def randomString = UUID.randomUUID().toString

  "Old profiles" should {

    "be migrated automatically" in new MigrationTestCase {
      // Given
      val taskRepository = TaskRepository
      val user1 = await(profileRepository.save(profile1, SaveMode.SignUp))
      val task1 = await(taskRepository.create(Task(text = randomString))(user1))
      val user2 = await(profileRepository.save(profile2, SaveMode.SignUp))
      val task2 = await(taskRepository.create(Task(text = randomString))(user2))
      def includeProfile(p: BasicProfile): Boolean = Seq(userId1, userId2).contains(p.userId)
      // When
      await(MongoUserService.migrateTaskOwnershipToUserObjectId(includeProfile))
      // Then
      val tasksForUser1 = await(taskRepository.findAll(user1))
      tasksForUser1 must contain(task1)
      val tasksForUser2 = await(taskRepository.findAll(user2))
      tasksForUser2 must contain(task2)
    }
  }
}
