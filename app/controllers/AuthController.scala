// app/controllers/AuthController.scala
package controllers

import models._
import play.api.libs.json._
import play.api.mvc._
import services.AuthService
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject()(
                                cc: ControllerComponents,
                                authService: AuthService
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val corsHeaders = Seq(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
    "Access-Control-Allow-Headers" -> "Content-Type, Authorization"
  )

  // Sign up endpoint
  def signUp(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[SignUpRequest].fold(
      errors => {
        val errorMessages = errors.map { case (path, validationErrors) =>
          s"$path: ${validationErrors.map(_.message).mkString(", ")}"
        }.mkString("; ")
        Future.successful(
          BadRequest(Json.obj("error" -> s"Invalid request: $errorMessages"))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        )
      },
      signUpRequest => {
        authService.signUp(signUpRequest).map {
          case Right(response) =>
            Ok(Json.toJson(response))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
          case Left(error) =>
            BadRequest(Json.obj("error" -> error))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
        }
      }
    )
  }

  // Login endpoint
  def login(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[LoginRequest].fold(
      errors => {
        val errorMessages = errors.map { case (path, validationErrors) =>
          s"$path: ${validationErrors.map(_.message).mkString(", ")}"
        }.mkString("; ")
        Future.successful(
          BadRequest(Json.obj("error" -> s"Invalid request: $errorMessages"))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        )
      },
      loginRequest => {
        authService.login(loginRequest).map {
          case Right(response) =>
            Ok(Json.toJson(response))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
          case Left(error) =>
            Unauthorized(Json.obj("error" -> error))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
        }
      }
    )
  }

  // Google OAuth endpoint
  def googleAuth(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[GoogleAuthRequest].fold(
      errors => {
        val errorMessages = errors.map { case (path, validationErrors) =>
          s"$path: ${validationErrors.map(_.message).mkString(", ")}"
        }.mkString("; ")
        Future.successful(
          BadRequest(Json.obj("error" -> s"Invalid request: $errorMessages"))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        )
      },
      googleRequest => {
        authService.googleAuth(googleRequest).map {
          case Right(response) =>
            Ok(Json.toJson(response))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
          case Left(error) =>
            Unauthorized(Json.obj("error" -> error))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
        }
      }
    )
  }

  // Verify token endpoint (for protected routes)
  def verifyToken(): Action[AnyContent] = Action.async { request =>
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        authService.verifyToken(token).map {
          case Right(user) =>
            Ok(Json.toJson(User.UserResponse.fromUser(user)))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
          case Left(error) =>
            Unauthorized(Json.obj("error" -> error))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
        }
      case _ =>
        Future.successful(
          Unauthorized(Json.obj("error" -> "Missing or invalid authorization header"))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        )
    }
  }

  // Get current user
  def me(): Action[AnyContent] = Action.async { request =>
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        authService.verifyToken(token).map {
          case Right(user) =>
            Ok(Json.toJson(User.UserResponse.fromUser(user)))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
          case Left(error) =>
            Unauthorized(Json.obj("error" -> error))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
        }
      case _ =>
        Future.successful(
          Unauthorized(Json.obj("error" -> "Not authenticated"))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        )
    }
  }

  // CORS preflight
  def options(path: String): Action[AnyContent] = Action { implicit request =>
    Ok("")
      .withHeaders(
        "Access-Control-Allow-Origin" -> "*",
        "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
        "Access-Control-Allow-Headers" -> "Content-Type, Authorization"
      )
  }
}