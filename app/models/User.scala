
// app/models/User.scala
package models

import play.api.libs.json._
import java.time.Instant
import java.util.UUID

case class User(
                 id: UUID,
                 email: String,
                 passwordHash: Option[String], // None for OAuth users
                 name: String,
                 provider: String, // "local" or "google"
                 providerId: Option[String], // Google ID for OAuth users
                 createdAt: Instant,
                 updatedAt: Instant
               )

object User {
  implicit val userFormat: Format[User] = Json.format[User]

  // Response without sensitive data
  case class UserResponse(
                           id: UUID,
                           email: String,
                           name: String,
                           provider: String
                         )

  object UserResponse {
    implicit val format: Format[UserResponse] = Json.format[UserResponse]

    def fromUser(user: User): UserResponse = UserResponse(
      id = user.id,
      email = user.email,
      name = user.name,
      provider = user.provider
    )
  }
}

// Request models
case class SignUpRequest(
                          email: String,
                          password: String,
                          name: String
                        )

object SignUpRequest {
  implicit val format: Format[SignUpRequest] = Json.format[SignUpRequest]
}

case class LoginRequest(
                         email: String,
                         password: String
                       )

object LoginRequest {
  implicit val format: Format[LoginRequest] = Json.format[LoginRequest]
}

case class GoogleAuthRequest(
                              token: String // Google ID token
                            )

object GoogleAuthRequest {
  implicit val format: Format[GoogleAuthRequest] = Json.format[GoogleAuthRequest]
}

case class AuthResponse(
                         token: String,
                         user: User.UserResponse
                       )

object AuthResponse {
  implicit val format: Format[AuthResponse] = Json.format[AuthResponse]
}