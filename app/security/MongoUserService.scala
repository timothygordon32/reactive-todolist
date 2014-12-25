package security

import models.User
import reactivemongo.api.indexes.Index
import repository.{ProfileRepository, TokenRepository}
import securesocial.core.services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoUserService extends UserService[User] with ProfileRepository with TokenRepository {
  override def indexes(): Future[Seq[Index]] = {
    val profileIndexes = super[ProfileRepository].indexes()
    val tokenIndexes = super[TokenRepository].indexes()
    Future.sequence(Seq(profileIndexes, tokenIndexes)).map(_.flatten)
  }
}
