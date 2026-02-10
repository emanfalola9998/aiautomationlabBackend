package models

import play.api.libs.json._
import java.time.Instant

case class Notification(
                         id: Int,
                         userId: String,
                         notificationType: String,
                         message: String,
                         link: Option[String],
                         isRead: Boolean,
                         createdAt: Instant,
                         triggeredByUserId: Option[String],
                         relatedBlogId: Option[String],
                         relatedCommentId: Option[Int]
                       )

object Notification {
  implicit val format: Format[Notification] = Json.format[Notification]
}