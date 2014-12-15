package repository

import org.joda.time.{DateTimeZone, DateTime}
import play.api.libs.json._

object Formats {
  implicit val dateTimeRead: Reads[DateTime] =
    (__ \ "$date").read[Long].map { dateTime =>
      new DateTime(dateTime, DateTimeZone.UTC)
    }

  implicit val dateTimeWrite: Writes[DateTime] = new Writes[DateTime] {
    def writes(dateTime: DateTime): JsValue = Json.obj(
      "$date" -> dateTime.getMillis
    )
  }

  val dateTimeFormat = Format[DateTime](dateTimeRead, dateTimeWrite)
}
