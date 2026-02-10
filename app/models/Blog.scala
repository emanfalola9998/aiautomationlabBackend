package models

import java.time.Instant
import play.api.libs.json.{Json, OFormat}

case class Blog(
                 id: String,
                 title: String,
                 content: String,
                 image: Option[String],
                 author: String,
                 likes: Int,
                 tags: Option[String],
                 datePublished: Instant
               )

object Blog {
  implicit val format: OFormat[Blog] = Json.format[Blog]
}