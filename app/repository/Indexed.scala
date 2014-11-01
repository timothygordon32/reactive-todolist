package repository

import play.api.Logger
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONInteger, BSONString}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Indexed {
  def toDescription(index: Index): String = index.name.fold("")(name => s"$name ") +
    index.key.map { case (field, indexType) => (field, toDescription(indexType))}.mkString("[", ",", "]")

  def toDescription(indexType: IndexType): String = indexType.value match {
    case BSONInteger(i) => i.toString
    case BSONString(s) => s
    case _ => throw new IllegalArgumentException("unsupported index type")
  }

  def ensureIndex(collection: JSONCollection, index: Index): Future[Unit] =
    collection.indexesManager.ensure(index).map {
      case true => Logger.info(s"Created index ${toDescription(index)} on ${collection.name}")
      case false => Logger.info(s"Index ${toDescription(index)} already exists on ${collection.name}")
    }
}
