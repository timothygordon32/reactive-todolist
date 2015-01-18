package repository

import org.joda.time.DateTime
import play.api.libs.json.Json

object AuthenticatorDetails {
  implicit val format = {
    implicit val dateTimeFormat = Formats.dateTimeFormat
    Json.format[AuthenticatorDetails]
  }
}

case class AuthenticatorDetails(id: String, expirationDate: DateTime, lastUsed: DateTime, creationDate: DateTime)

