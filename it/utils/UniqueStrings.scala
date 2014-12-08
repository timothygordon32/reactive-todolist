package utils

import java.util.UUID

trait UniqueStrings {
  def uniqueString = UUID.randomUUID.toString
}
