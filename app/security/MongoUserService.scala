package security

import models.User
import reactivemongo.api.indexes.Index
import repository.{MongoTokenRepositoryComponent, ProfileRepository}
import securesocial.core.services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SecureSocialUserServiceComponent {
  val userService: UserService[User]
}

trait MongoSecureSocialUserServiceComponent extends SecureSocialUserServiceComponent {
  self: MongoTokenRepositoryComponent =>

  class MongoUserService extends UserService[User] with ProfileRepository with MongoTokenRepository {
    override def indexes(): Future[Seq[Index]] = {
      val profileIndexes = super[ProfileRepository].indexes()
      val tokenIndexes = super[MongoTokenRepository].indexes()
      Future.sequence(Seq(profileIndexes, tokenIndexes)).map(_.flatten)
    }
  }
}

