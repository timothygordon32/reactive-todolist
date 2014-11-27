package security

import models.User
import play.api.{Logger, Play}
import play.api.Play.current
import play.api.libs.iteratee.{Iteratee, Enumerator}
import reactivemongo.api.indexes.Index
import repository.{TaskRepository, ProfileRepository, TokenRepository}
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.{SaveMode, UserService}
import securesocial.core.{AuthenticationMethod, BasicProfile, PasswordInfo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoUserService extends UserService[User] with ProfileRepository with TokenRepository {
  def migrateTaskOwnershipToUserObjectId(include: BasicProfile => Boolean) = {
    Logger.info(s"Migrating tasks to ownership by user ObjectId")
    def migrate(total: Int, profile: BasicProfile): Future[Int] = if (include(profile)) {
      findKnown(profile.userId).flatMap { user =>
        TaskRepository.migrateTaskOwnershipToUserObjectId(user)
      }
    } else Future.successful(total)

    val profiles = enumerateProfiles
    val migration = Iteratee.foldM(0)(migrate)

    profiles run migration
  } map { migrated =>
    Logger.info(s"Migrated $migrated task(s) to ownership by user ObjectId")
  }

  def migrateProfileUsernameToEmail(include: BasicProfile => Boolean) = {
    Logger.info(s"Migrating profiles to use email as username")
    def migrate(total: Int, oldProfile: BasicProfile): Future[Int] = oldProfile.email match {
      case None => Future.successful(total)
      case Some(email) =>
        if (include(oldProfile)) {
          find(UsernamePasswordProvider.UsernamePassword, email).flatMap {
            case Some(_) => delete(oldProfile).map(_ => total + 1)
            case None => for {
              _ <- TaskRepository.copy(oldProfile.userId, email)
              _ <- save(oldProfile.copy(userId = email), SaveMode.SignUp)
              _ <- delete(oldProfile)
            }
            yield total + 1
          }
        } else Future.successful(total)
    }

    val oldProfiles = Enumerator.generateM(findOldProfile)
    val migration = Iteratee.foldM(0)(migrate)

    oldProfiles run migration
  } map { migrated =>
    Logger.info(s"Migrated $migrated profile(s) to use email as username")
    migrated
  }

  override def indexes(): Future[Seq[Index]] = {
    val profileIndexes = super[ProfileRepository].indexes()
    val tokenIndexes = super[TokenRepository].indexes()
    Future.sequence(Seq(profileIndexes, tokenIndexes)).map(_.flatten)
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
        _ <- TaskRepository.copy(oldProfile.userId, email)
        _ <- save(oldProfile.copy(userId = email), SaveMode.SignUp)
        _ <- delete(oldProfile)
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
