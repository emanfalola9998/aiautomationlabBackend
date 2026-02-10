package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import repository.CommentsRepository
import models.Comments

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import services.NotificationService
import utils.JwtUtil

@Singleton
class CommentsController @Inject()(
                                    notififcationService: NotificationService,
                                    jwtUtil: JwtUtil,
                                    cc: ControllerComponents,
                                    commentsRepo: CommentsRepository
                                  )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // CORS headers helper
  private val corsHeaders = Seq(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
    "Access-Control-Allow-Headers" -> "Content-Type, Authorization"
  )

  // Create a new comment
  def createComment(blogId: String) = Action.async(parse.json) { req =>
    req.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        jwtUtil.getUserIdFromToken(token) match {
          case Some(userId) =>
            val username = (req.body \ "username").as[String]
            val comment = (req.body \ "comment").as[String]
            val rating = (req.body \ "rating").as[Int]

            val newComment = Comments(
              blogId = blogId,
              id = 0,
              username = username,
              comment = comment,
              timestamp = Instant.now(),
              rating = rating
            )

            commentsRepo.createComment(newComment).flatMap { created =>
              // Create notification for blog author
              notififcationService.notifyNewComment(blogId, userId.toString, created.id).map { _ =>
                Created(Json.toJson(created)).withHeaders(corsHeaders: _*)
              }
            }.recover {
              case e: Exception =>
                InternalServerError(Json.obj("error" -> s"Failed to create comment: ${e.getMessage}"))
                  .withHeaders(corsHeaders: _*)
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

  // Get all comments for a blog
  def getComments(blogId: String) = Action.async {
    commentsRepo.getForBlog(blogId).map(comments => Ok(Json.toJson(comments)).withHeaders(corsHeaders: _*))
  }

  // Get a single comment
  def getComment(blogId: String, id: Int) = Action.async {
    commentsRepo.getOne(blogId, id).map {
      case Some(comment) => Ok(Json.toJson(comment))
      case None          => NotFound(Json.obj("error" -> "Comment not found"))
    }
  }

  // Delete a comment
  def deleteComment(blogId: String, id: Int) = Action.async {
    commentsRepo.delete(blogId, id).map {
      case 0 => NotFound(Json.obj("error" -> "Comment not found")).withHeaders(corsHeaders: _*)
      case _ => Ok(Json.obj("status" -> "deleted")).withHeaders(corsHeaders: _*)
    }
  }

  // app/controllers/CommentsController.scala
  // app/controllers/CommentsController.scala
  def updateRating(blogId: String, commentId: Int) = Action.async(parse.json) { req =>
    val newRating = (req.body \ "rating").as[Int]

    commentsRepo.getOne(blogId, commentId).flatMap {
      case Some(comment) =>
        val updated = comment.copy(rating = newRating)
        commentsRepo.update(updated).map { _ =>
          Ok(Json.toJson(updated)).withHeaders(corsHeaders: _*)
        }
      case None =>
        Future.successful(
          NotFound(Json.obj("error" -> "Comment not found")).withHeaders(corsHeaders: _*)
        )
    }.recover {
      case e: Exception =>
        InternalServerError(Json.obj("error" -> s"Failed to update rating: ${e.getMessage}"))
          .withHeaders(corsHeaders: _*)
    }
  }



//  def options(): Action[AnyContent] = Action { implicit request =>
//    Ok("")
//      .withHeaders(
//        "Access-Control-Allow-Origin" -> "*",
//        "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
//        "Access-Control-Allow-Headers" -> "Content-Type, Authorization"
//      )
//  }

  def options(path: String): Action[AnyContent] = Action { implicit request =>
    Ok("").withHeaders(corsHeaders: _*)
  }

}
