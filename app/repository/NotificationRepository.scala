package repositories

import models.Notification
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationRepository @Inject()(
                                        protected val dbConfigProvider: DatabaseConfigProvider
                                      )(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private class NotificationsTable(tag: Tag) extends Table[Notification](tag, "notifications") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[String]("user_id")
    def notificationType = column[String]("type")
    def message = column[String]("message")
    def link = column[Option[String]]("link")
    def isRead = column[Boolean]("is_read")
    def createdAt = column[Instant]("created_at")
    def triggeredByUserId = column[Option[String]]("triggered_by_user_id")
    def relatedBlogId = column[Option[String]]("related_blog_id")
    def relatedCommentId = column[Option[Int]]("related_comment_id")

    def * = (id, userId, notificationType, message, link, isRead, createdAt,
      triggeredByUserId, relatedBlogId, relatedCommentId) <>
      ((Notification.apply _).tupled, Notification.unapply)
  }

  private val notifications = TableQuery[NotificationsTable]

  // Create notification
  def create(notification: Notification): Future[Notification] = {
    val insertQuery = (notifications returning notifications.map(_.id)
      into ((notif, id) => notif.copy(id = id))) += notification
    db.run(insertQuery)
  }

  // Get user's notifications
  def getByUserId(userId: String, limit: Int = 20): Future[Seq[Notification]] = {
    db.run(
      notifications
        .filter(_.userId === userId)
        .sortBy(_.createdAt.desc)
        .take(limit)
        .result
    )
  }

  // Get unread count
  def getUnreadCount(userId: String): Future[Int] = {
    db.run(
      notifications
        .filter(n => n.userId === userId && !n.isRead)
        .length
        .result
    )
  }

  // Mark as read
  def markAsRead(id: Int): Future[Int] = {
    db.run(
      notifications
        .filter(_.id === id)
        .map(_.isRead)
        .update(true)
    )
  }

  // Mark all as read for user
  def markAllAsRead(userId: String): Future[Int] = {
    db.run(
      notifications
        .filter(_.userId === userId)
        .map(_.isRead)
        .update(true)
    )
  }
}