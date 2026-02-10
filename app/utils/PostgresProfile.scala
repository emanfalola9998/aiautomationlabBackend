package utils

import com.github.tminglei.slickpg._
import slick.basic.Capability
import slick.jdbc.PostgresProfile

trait MyPostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support      // Instant / Timestamp support
  with PgPlayJsonSupport   // JSON support if needed
{
  def pgjson = "jsonb"    // use JSONB type in Postgres

  override val api: API = new API {}

  trait API extends super.API
    with ArrayImplicits
    with DateTimeImplicits
    with PlayJsonImplicits
}

object MyPostgresProfile extends MyPostgresProfile
