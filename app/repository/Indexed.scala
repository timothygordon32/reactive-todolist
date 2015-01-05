package repository

import play.api.Logger
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONDouble, BSONInteger, BSONString}
import reactivemongo.core.commands.{BSONCommandResultMaker, Command, CommandError}

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

  def dropIndex(collection: JSONCollection, index: Index): Future[Unit] =
    collection.db.command(DropIndex(collection.name, index.eventualName)).map { indexNo =>
      Logger.info(s"Dropped index ${toDescription(index)} on ${collection.name}, was: $indexNo")
    } recoverWith {
      case e: CommandError => Future.successful(Logger.warn(e.message))
    }
}

case class DropIndex(collection: String,
                     index: String) extends Command[Int] {
  override def makeDocuments = BSONDocument(
    "dropIndexes" -> BSONString(collection),
    "index" -> BSONString(index))

  object ResultMaker extends BSONCommandResultMaker[Int] {
    def apply(document: BSONDocument) = {
      def error: (BSONDocument, Option[String]) => CommandError =
        (doc, name) => {
          val command = name.map(" " + _).getOrElse("")
          val errorMessage = doc.getAs[String]("errmsg").getOrElse("of an unknown error")
          CommandError(s"Command$command failed because $errorMessage", Some(doc))
        }

      CommandError.checkOk(document, Some("dropIndexes"), error)
        .toLeft(document.getAs[BSONDouble]("nIndexesWas").map(_.value.toInt).get)
    }
  }

}
