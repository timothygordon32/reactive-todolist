package repository

import play.api.test.{WithApplication, PlaySpecification}
import reactivemongo.api.indexes.{IndexType, Index}

class TaskRepositoryUserIndexSpec extends PlaySpecification {

  "User task repository" should {

    "have an index on user" in new WithApplication {
      // Given
      val repo = TaskRepository
      // When
      val indexes = await(TaskRepository.indexes())
      // Then
      indexes must contain((i: Index) => i.key must beEqualTo(Seq("user" -> IndexType.Ascending)))
    }
  }
}