package security

import models.User
import play.api.Play
import play.api.Play.current
import reactivemongo.api.indexes.Index
import repository.{TaskRepository, ProfileRepository, TokenRepository}
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.{SaveMode, UserService}
import securesocial.core.{AuthenticationMethod, BasicProfile, PasswordInfo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoUserService extends UserService[User] with ProfileRepository with TokenRepository {

  override def indexes(): Future[List[Index]] = {
    val profileIndexes = super[ProfileRepository].indexes()
    val tokenIndexes = super[TokenRepository].indexes()
    Future.sequence(List(profileIndexes, tokenIndexes)).map(_.flatten)
  }

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    super.find(providerId, userId).flatMap {
      case Some(profile) => Future.successful(Some(profile))
      case None => attemptMigration(providerId, userId)
    }
  }

  private def attemptMigration(providerId: String, email: String): Future[Option[BasicProfile]] = {
    findByEmailAndProvider(email, providerId).flatMap {
      case None => Future.successful(None)
      case Some(oldProfile) => for {
        _ <- save(oldProfile.copy(userId = email), SaveMode.SignUp)
        _ <- TaskRepository.copy(User(oldProfile.userId, None), User(email, None))
        newProfile <- super.find(providerId, email)
      }
      yield newProfile
    }
  }

  private def seedTestUser: Future[_] = find(UsernamePasswordProvider.UsernamePassword, "testuser1@nomail.com").flatMap {
    profile =>
      if (!profile.isDefined) {
        for {
          _ <- save("testuser1@nomail.com", "secret1", "Test1")
          _ <- save("testuser2@nomail.com", "secret2", "Test2")
        } yield ()
      }
      else Future.successful(())
  }

  private def save(userId: String, password: String, firstName: String): Future[User] = {
    save(BasicProfile(
      providerId = UsernamePasswordProvider.UsernamePassword,
      userId = userId,
      firstName = Some(firstName),
      lastName = None,
      fullName = None,
      email = Some(userId),
      avatarUrl = None,
      authMethod = AuthenticationMethod.UserPassword,
      passwordInfo = Some(PasswordInfo(PasswordHasher.id, PasswordHash.hash(password)))),
      SaveMode.SignUp)
  }

  if (!Play.isProd) seedTestUser
}
