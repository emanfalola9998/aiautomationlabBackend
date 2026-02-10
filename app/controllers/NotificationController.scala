// app/controllers/NotificationController.scala
package controllers

import models.Notification
import play.api.libs.json._
import play.api.mvc._
import repositories.NotificationRepository
import utils.JwtUtil
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging


@Singleton
class NotificationController @Inject()(
                                        cc: ControllerComponents,
                                        notificationRepo: NotificationRepository,
                                        jwtUtil: JwtUtil
                                      )(implicit ec: ExecutionContext) extends AbstractController(cc) with Logging   {

  private val corsHeaders = Seq(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
    "Access-Control-Allow-Headers" -> "Content-Type, Authorization"
  )

  // Get user's notifications
  def getNotifications(): Action[AnyContent] = Action.async { request =>
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)

        jwtUtil.getUserIdFromToken(token) match {
          case Some(userId) =>
            notificationRepo.getByUserId(userId.toString).map { notifications =>
              notifications.foreach(n => println(s"  - ${n.message}"))
              Ok(Json.toJson(notifications)).withHeaders(corsHeaders: _*)
            }
          case None =>
            Future.successful(Unauthorized(Json.obj("error" -> "Invalid token"))
              .withHeaders(corsHeaders: _*))
        }
      case _ =>
        Future.successful(Unauthorized(Json.obj("error" -> "Missing authorization"))
          .withHeaders(corsHeaders: _*))
    }
  }

  // Get unread count
  def getUnreadCount: Action[AnyContent] = Action.async { request =>
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)

        jwtUtil.getUserIdFromToken(token) match {
          case Some(userId) =>
            notificationRepo.getUnreadCount(userId.toString).map { count =>
              Ok(Json.obj("count" -> count)).withHeaders(corsHeaders: _*)
            }
          case None =>
            Future.successful(Unauthorized(Json.obj("error" -> "Invalid token"))
              .withHeaders(corsHeaders: _*))
        }
      case _ =>
        Future.successful(Unauthorized(Json.obj("error" -> "Missing authorization"))
          .withHeaders(corsHeaders: _*))
    }
  }

  // Mark notification as read
  def markAsRead(id: Int): Action[AnyContent] = Action.async { request =>
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        jwtUtil.getUserIdFromToken(token) match {
          case Some(_) =>
            notificationRepo.markAsRead(id).map { _ =>
              Ok(Json.obj("success" -> true)).withHeaders(corsHeaders: _*)
            }
          case None =>
            Future.successful(Unauthorized(Json.obj("error" -> "Invalid token"))
              .withHeaders(corsHeaders: _*))
        }
      case _ =>
        println("ERROR: Missing or invalid Authorization header")
        Future.successful(Unauthorized(Json.obj("error" -> "Missing authorization"))
          .withHeaders(corsHeaders: _*))
    }
  }

  // Mark all as read
  def markAllAsRead(): Action[AnyContent] = Action.async { request =>
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        jwtUtil.getUserIdFromToken(token) match {
          case Some(userId) =>
            notificationRepo.markAllAsRead(userId.toString).map { _ =>
              Ok(Json.obj("success" -> true)).withHeaders(corsHeaders: _*)
            }
          case None =>
            Future.successful(Unauthorized(Json.obj("error" -> "Invalid token"))
              .withHeaders(corsHeaders: _*))
        }
      case _ =>
        Future.successful(Unauthorized(Json.obj("error" -> "Missing authorization"))
          .withHeaders(corsHeaders: _*))
    }
  }

  def options(path: String): Action[AnyContent] = Action { implicit request =>
    Ok("")
      .withHeaders(corsHeaders: _*)
  }
}