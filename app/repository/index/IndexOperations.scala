package repository.index

import reactivemongo.api.indexes.Index

sealed trait IndexOperations

case class Drop(index: Index) extends IndexOperations

case class Ensure(index: Index) extends IndexOperations
