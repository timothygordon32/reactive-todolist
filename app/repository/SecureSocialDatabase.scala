package repository

import play.api.Play
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import scala.concurrent.ExecutionContext.Implicits.global

trait SecureSocialDatabase {
  lazy val secureSocialDatabase = Play.current.configuration.getString("secureSocialDatabaseName")

  lazy val db: DB = {
    val rootDB = ReactiveMongoPlugin.db
    secureSocialDatabase.map(rootDB.sibling(_)).getOrElse(rootDB)
  }
}
