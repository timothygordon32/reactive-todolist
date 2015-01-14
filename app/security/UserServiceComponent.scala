package security

import models.User
import reactivemongo.api.indexes.Index
import repository.{MongoProfileRepositoryComponent, MongoTokenRepositoryComponent}
import securesocial.core.services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserServiceComponent {
  val userService: UserService[User]
}

trait MongoUserServiceComponent extends UserServiceComponent {
  self: MongoTokenRepositoryComponent with MongoProfileRepositoryComponent =>

  class MongoUserService extends UserService[User] with MongoProfileRepository with MongoTokenRepository {
    override def indexes(): Future[Seq[Index]] = {
      val profileIndexes = super[MongoProfileRepository].indexes()
      val tokenIndexes = super[MongoTokenRepository].indexes()
      Future.sequence(Seq(profileIndexes, tokenIndexes)).map(_.flatten)
    }
  }
}

