package repository

import play.api.Logger
import reactivemongo.api.indexes.{CollectionIndexesManager, Index, IndexType}
import reactivemongo.bson.{BSONInteger, BSONString}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Indexed {
  def indexesManager: CollectionIndexesManager

  def toDescription(index: Index): String = index.name.getOrElse {
    index.key.map { case (field, indexType) => (field, toDescription(indexType))}.toString()
  }

  def toDescription(indexType: IndexType): String = indexType.value match {
    case BSONInteger(i) => i.toString
    case BSONString(s) => s
    case _ => throw new IllegalArgumentException("unsupported index type")
  }

  def ensureIndex(index: Index): Future[Unit] =
    indexesManager.ensure(index).map {
      case true => Logger.info(s"Created index [${toDescription(index)}] for profile repository")
      case false => Logger.info(s"Index [${toDescription(index)}] not created for profile repository")
    }
}
