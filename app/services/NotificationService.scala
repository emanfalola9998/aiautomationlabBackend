package services

import models.{Notification, Blog, Comments}
import repositories.{NotificationRepository, UserRepository}
import repository.BlogRepository
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationService @Inject()(
                                     notificationRepo: NotificationRepository,
                                     blogRepo: BlogRepository,
                                     userRepo: UserRepository
                                   )(implicit ec: ExecutionContext) {

  // Create notification when someone likes a blog
  def notifyBlogLiked(blogId: String, likedByUserId: String): Future[Option[Notification]] = {
    println(s"=== NOTIFY BLOG LIKED ===")
    println(s"Blog ID: $blogId")
    println(s"Liked by: $likedByUserId")

    blogRepo.getById(blogId).flatMap {
      case Some(blog) =>
        println(s"Blog found! Author: ${blog.authorId}")

        if (blog.authorId != likedByUserId) {  // Don't notify if user likes their own blog
          println(s"Creating notification for blog author: ${blog.authorId}")

          val notification = Notification(
            id = 0,
            userId = blog.authorId,  // ✅ CORRECT - Send notification TO the blog author
            notificationType = "blog_like",
            message = "Someone liked your blog post",
            link = Some(s"/blog/$blogId"),
            isRead = false,
            createdAt = Instant.now(),
            triggeredByUserId = Some(likedByUserId),  // ✅ This is who triggered it
            relatedBlogId = Some(blogId),
            relatedCommentId = None
          )

          notificationRepo.create(notification).map { created =>
            println(s"Notification created for user: ${created.userId}")
            Some(created)
          }
        } else {
          println("Not creating notification - user liked their own blog")
          Future.successful(None)
        }

      case None =>
        println(s"Blog not found: $blogId")
        Future.successful(None)
    }
  }

  // Create notification when someone comments on a blog
  def notifyNewComment(blogId: String, commentedByUserId: String, commentId: Int): Future[Option[Notification]] = {
    blogRepo.getById(blogId).flatMap {
      case Some(blog) if blog.authorId != commentedByUserId =>
        val notification = Notification(
          id = 0,
          userId = blog.authorId,  // ✅ Send TO the blog author
          notificationType = "new_comment",
          message = "Someone commented on your blog post",
          link = Some(s"/blog/$blogId"),
          isRead = false,
          createdAt = Instant.now(),
          triggeredByUserId = Some(commentedByUserId),  // ✅ Who triggered it
          relatedBlogId = Some(blogId),
          relatedCommentId = Some(commentId)
        )
        notificationRepo.create(notification).map(Some(_))
      case _ => Future.successful(None)
    }
  }

  // Create notification when someone likes a comment
  def notifyCommentLiked(commentId: Int, blogId: String, likedByUserId: String): Future[Option[Notification]] = {
    Future.successful(None)
  }
}