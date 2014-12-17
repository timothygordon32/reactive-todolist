package time

import org.joda.time.{DateTimeZone, DateTime}

object DateTimeUtils {
  def now = DateTime.now(DateTimeZone.UTC)
}
