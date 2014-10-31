package repository

import play.api.Play

trait SecureSocialDatabase {
  lazy val secureSocialDatabase = Play.current.configuration.getString("secureSocialDatabaseName")
    .getOrElse(throw new IllegalArgumentException("Specify a name for the security database"))
}
