package forex.services.time

import java.time.OffsetDateTime

trait Clock {
  def now(): OffsetDateTime
}

object Clock {
  def apply(): Clock = new Clock {
    override def now(): OffsetDateTime = OffsetDateTime.now()
  }
}