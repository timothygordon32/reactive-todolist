package repository.index

import play.api.Play

import scala.concurrent.duration._

trait DelayedIndexOperations {
  implicit lazy val delay: IndexOperationDelay = IndexOperationDelay(
    Play.current.configuration.getMilliseconds("mongodb.indexOperationDelay").getOrElse(0L) milliseconds)
}
