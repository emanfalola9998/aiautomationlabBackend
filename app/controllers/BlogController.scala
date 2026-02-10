// app/controllers/BlogController.scala
package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import repository.BlogRepository
import models.Blog
import utils.JwtUtil
import java.util.UUID
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import models.BlogWithCommentsResponse
import services.NotificationService

@Singleton
class BlogController @Inject()(
                                cc: ControllerComponents,
                                blogRepo: BlogRepository,
                                jwtUtil: JwtUtil,
                                notificationService: NotificationService
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // CORS headers helper
  private val corsHeaders = Seq(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
    "Access-Control-Allow-Headers" -> "Content-Type, Authorization"
  )

  def getAll = Action.async {
    blogRepo.getAll.map(blogs => Ok(Json.toJson(blogs)).withHeaders(corsHeaders: _*))
  }

  def getById(id: String) = Action.async {
    blogRepo.getById(id).map {
      case Some(blog) => Ok(Json.toJson(blog)).withHeaders(corsHeaders: _*)
      case None       => NotFound(Json.obj("error" -> "Blog not found")).withHeaders(corsHeaders: _*)
    }
  }

  // Updated create method with authentication
  def create = Action(parse.json).async { req =>
    // Check authentication
    req.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        jwtUtil.getUserIdFromToken(token) match {
          case Some(userId) =>
            // User is authenticated, create blog
            try {
              val title = (req.body \ "title").as[String]
              val content = (req.body \ "content").as[String]
              val image = (req.body \ "image").asOpt[String].getOrElse("")
              val tags = (req.body \ "tags").asOpt[String].getOrElse("")
              val author = (req.body \ "author").as[String]
              val likes = (req.body \ "likes").asOpt[Int].getOrElse(0)

              val blog = Blog(
                id = UUID.randomUUID().toString,
                title = title,
                content = content,
                author = author, // Use authenticated user ID
                likes = likes,
                image = Some(image),
                tags = Some(tags),
                datePublished = Instant.now()
              )

              blogRepo.create(blog).map { _ =>
                Created(Json.toJson(blog)).withHeaders(corsHeaders: _*)
              }.recover {
                case e: Exception =>
                  InternalServerError(Json.obj("error" -> s"Failed to create blog: ${e.getMessage}"))
                    .withHeaders(corsHeaders: _*)
              }
            } catch {
              case e: Exception =>
                Future.successful(
                  BadRequest(Json.obj("error" -> s"Invalid request: ${e.getMessage}"))
                    .withHeaders(corsHeaders: _*)
                )
            }

          case None =>
            Future.successful(
              Unauthorized(Json.obj("error" -> "Invalid token"))
                .withHeaders(corsHeaders: _*)
            )
        }

      case _ =>
        Future.successful(
          Unauthorized(Json.obj("error" -> "Missing authorization header"))
            .withHeaders(corsHeaders: _*)
        )
    }
  }


  // Add this method to your BlogController
  def likeBlog(id: String) = Action.async { req =>
    req.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        val authorName = jwtUtil.getAuthorFromToken(token)
        jwtUtil.getUserIdFromToken(token) match {
          case Some(userId) =>
            blogRepo.getById(id).flatMap {
              case Some(blog) =>
                val updatedBlog = blog.copy(likes = blog.likes + 1)
                blogRepo.update(id, updatedBlog).flatMap { _ =>
                  // Create notification for blog author
                  notificationService.notifyBlogLiked(id, userId.toString).map { _ =>
                    Ok(Json.toJson(updatedBlog)).withHeaders(corsHeaders: _*)
                  }
                }
              case None =>
                Future.successful(
                  NotFound(Json.obj("error" -> "Blog not found")).withHeaders(corsHeaders: _*)
                )
            }
          case None =>
            Future.successful(
              Unauthorized(Json.obj("error" -> "Invalid token")).withHeaders(corsHeaders: _*)
            )
        }
      case _ =>
        Future.successful(
          Unauthorized(Json.obj("error" -> "Missing authorization")).withHeaders(corsHeaders: _*)
        )
    }
  }

  def getBlogWithComments(id: String) = Action.async {
    blogRepo.getBlogWithComments(id).map {
      case (Some(blog), comments) =>
        Ok(Json.toJson(BlogWithCommentsResponse(blog, comments))).withHeaders(corsHeaders: _*)

      case (None, _) =>
        NotFound(Json.obj("error" -> "Blog not found")).withHeaders(corsHeaders: _*)
    }
  }

  def options(path: String): Action[AnyContent] = Action { implicit request =>
    Ok("").withHeaders(corsHeaders: _*)
  }
}