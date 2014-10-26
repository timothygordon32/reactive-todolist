package repository

import play.api.test.{PlaySpecification, WithApplication}
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.indexes.IndexType

class TokenRepositoryIndexesSpec extends PlaySpecification {

  "Token repository" should {

    "have an index on uuid" in new WithApplication {
      val repo = new TokenRepository {
        override def db = ReactiveMongoPlugin.db
      }
      val indexes = await(repo.indexes())
      indexes.filter(_.key == Seq("uuid" -> IndexType.Ascending)) must not be empty
    }

    "have an index on expiration time" in new WithApplication {
      val repo = new TokenRepository {
        override def db = ReactiveMongoPlugin.db
      }
      val indexes = await(repo.indexes())
      indexes.filter(_.key == Seq("expirationTime" -> IndexType.Ascending)) must not be empty
    }
  }
}
