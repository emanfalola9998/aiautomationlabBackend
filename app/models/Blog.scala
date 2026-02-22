package models

import java.time.Instant
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Blog(
                 id: String,
                 title: String,
                 content: String,
                 image: Option[String],
                 authorId: String,      // User ID (for notifications/logic)
                 authorName: String,    // Display name (for UI)
                 likes: Int,
                 likedBy: Set[String],
                 tags: Option[String],
                 datePublished: Instant
               )

object Blog {
  implicit val reads: Reads[Blog] = (
    (__ \ "id").read[String] and
    (__ \ "title").read[String] and
    (__ \ "content").read[String] and
    (__ \ "image").readNullable[String] and
    (__ \ "authorId").read[String] and
    (__ \ "authorName").read[String] and
    (__ \ "likes").read[Int] and
    (__ \ "likedBy").read[Set[String]] and
    (__ \ "tags").readNullable[String] and
    (__ \ "datePublished").read[Instant]
  )(Blog.apply _)

  implicit val writes: Writes[Blog] = (
    (__ \ "id").write[String] and
    (__ \ "title").write[String] and
    (__ \ "content").write[String] and
    (__ \ "image").writeNullable[String] and
    (__ \ "authorId").write[String] and
    (__ \ "author").write[String] and      // Output authorName as "author" for frontend
    (__ \ "likes").write[Int] and
    (__ \ "likedBy").write[Set[String]] and
    (__ \ "tags").writeNullable[String] and
    (__ \ "datePublished").write[Instant]
  )(unlift(Blog.unapply))
}