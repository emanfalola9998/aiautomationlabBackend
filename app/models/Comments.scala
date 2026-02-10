package models

import play.api.libs.json.{Json, OFormat}

import java.time.Instant

case class Comments(
                     blogId: String,
                     id: Int,
                     username: String,
                     comment: String,
                     timestamp: Instant,
                     rating: Int
                   )

object Comments {
  implicit val format: OFormat[Comments] = Json.format[Comments]
}