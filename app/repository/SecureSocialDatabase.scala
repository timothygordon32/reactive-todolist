package repository

import play.api.Play
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import scala.concurrent.ExecutionContext.Implicits.global

trait SecureSocialDatabase {
  lazy val secureSocialDatabase = Play.current.configuration.getString("secureSocialDatabaseName")

  lazy val db: DB = secureSocialDatabase.map(securityDb).getOrElse(ReactiveMongoPlugin.db)

  def securityDb(name: String): DB = ReactiveMongoPlugin.db.sibling(name)
}
