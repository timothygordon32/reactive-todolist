package repository.index

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.Index

import scala.concurrent.{ExecutionContext, Future}

class IndexedCollection(val collection: JSONCollection, val operations: Seq[IndexOperations])(implicit d: IndexOperationDelay, ec: ExecutionContext) extends Indexed {
  def delay: IndexOperationDelay = d

  private val indexOperations = operations.map {
    case Ensure(index) => ensureIndex(collection, index)
    case Drop(index) => dropIndex(collection, index)
  }

  def indexes(): Future[Seq[Index]] = for {
    _ <- Future.sequence(indexOperations)
    indexes <- collection.indexesManager.list()
  } yield indexes
}
