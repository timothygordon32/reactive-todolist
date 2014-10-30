package security

import models.User
import play.api.Play
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import repository.{ProfileRepository, TokenRepository}
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.{PasswordInfo, AuthenticationMethod, BasicProfile}
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoUserService extends UserService[User] with ProfileRepository with TokenRepository {
  override def db: DB = ReactiveMongoPlugin.db

  override def indexes(): Future[List[Index]] = {
    val profileIndexes = super[ProfileRepository].indexes()
    val tokenIndexes = super[TokenRepository].indexes()
    Future.sequence(List(profileIndexes, tokenIndexes)).map(_.flatten)
  }

  private def seedTestUser: Future[_] = find(UsernamePasswordProvider.UsernamePassword, "testuser1").flatMap { profile =>
    if (!profile.isDefined) {
      for {
        _ <- save("testuser1", "secret1")
        _ <- save("testuser2", "secret2")
      } yield ()
    }
    else Future.successful(())
  }

  private def save(userId: String, password: String): Future[User] = {
    save(BasicProfile(
      providerId = UsernamePasswordProvider.UsernamePassword,
      userId = userId,
      firstName = None,
      lastName = None,
      fullName = None,
      email = Some(s"$userId@nomail.com"),
      avatarUrl = None,
      authMethod = AuthenticationMethod.UserPassword,
      passwordInfo = Some(PasswordInfo(PasswordHasher.id, PasswordHash.hash(password)))),
      SaveMode.SignUp)
  }

  if (!Play.isProd) seedTestUser
}
