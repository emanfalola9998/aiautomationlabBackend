// app/services/AuthService.scala
package services

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import models._
import org.mindrot.jbcrypt.BCrypt
import play.api.Configuration
import repositories.UserRepository
import utils.JwtUtil
import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Try, Success, Failure}

@Singleton
class AuthService @Inject()(
                             userRepository: UserRepository,
                             jwtUtil: JwtUtil,
                             config: Configuration
                           )(implicit ec: ExecutionContext) {

  private val googleClientId = config.get[String]("google.clientId")

  private val verifier = new GoogleIdTokenVerifier.Builder(
    new NetHttpTransport(),
    GsonFactory.getDefaultInstance()
  )
    .setAudience(List(googleClientId).asJava)
    .build()

  // Sign up with email/password
  def signUp(request: SignUpRequest): Future[Either[String, AuthResponse]] = {
    userRepository.emailExists(request.email).flatMap {
      case true => Future.successful(Left("Email already exists"))
      case false =>
        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val now = Instant.now
        val user = User(
          id = UUID.randomUUID(),
          email = request.email.toLowerCase,
          passwordHash = Some(passwordHash),
          name = request.name,
          provider = "local",
          providerId = None,
          createdAt = now,
          updatedAt = now
        )

        userRepository.create(user).map { createdUser =>
          val token = jwtUtil.generateToken(createdUser.id, createdUser.email)
          Right(AuthResponse(token, User.UserResponse.fromUser(createdUser)))
        }
    }
  }

  // Login with email/password
  def login(request: LoginRequest): Future[Either[String, AuthResponse]] = {
    userRepository.findByEmail(request.email.toLowerCase).map {
      case Some(user) if user.provider == "local" =>
        user.passwordHash match {
          case Some(hash) if BCrypt.checkpw(request.password, hash) =>
            val token = jwtUtil.generateToken(user.id, user.email)
            Right(AuthResponse(token, User.UserResponse.fromUser(user)))
          case _ =>
            Left("Invalid credentials")
        }
      case Some(user) =>
        Left(s"Please login with ${user.provider}")
      case None =>
        Left("Invalid credentials")
    }
  }

  // Google OAuth login
  def googleAuth(request: GoogleAuthRequest): Future[Either[String, AuthResponse]] = {
    Future {
      Try {
        val idToken = verifier.verify(request.token)

        if (idToken == null) {
          Left("Invalid Google token")
        } else {
          val payload = idToken.getPayload
          val googleId = payload.getSubject
          val email = payload.getEmail.toLowerCase
          val name = Option(payload.get("name")).map(_.toString).getOrElse("User")

          Right((googleId, email, name))
        }
      } match {
        case Success(result) => result
        case Failure(e) =>
          println(s"Google token verification failed: ${e.getMessage}")
          Left("Invalid Google token")
      }
    }.flatMap {
      case Left(error) => Future.successful(Left(error))
      case Right((googleId, email, name)) =>
        // Check if user exists by provider ID
        userRepository.findByProviderId("google", googleId).flatMap {
          case Some(user) =>
            // User exists, return token
            val token = jwtUtil.generateToken(user.id, user.email)
            Future.successful(Right(AuthResponse(token, User.UserResponse.fromUser(user))))

          case None =>
            // Check if email exists with different provider
            userRepository.findByEmail(email).flatMap {
              case Some(existingUser) =>
                Future.successful(Left(s"Email already registered with ${existingUser.provider}"))

              case None =>
                // Create new user
                val now = Instant.now
                val newUser = User(
                  id = UUID.randomUUID(),
                  email = email,
                  passwordHash = None,
                  name = name,
                  provider = "google",
                  providerId = Some(googleId),
                  createdAt = now,
                  updatedAt = now
                )

                userRepository.create(newUser).map { createdUser =>
                  val token = jwtUtil.generateToken(createdUser.id, createdUser.email)
                  Right(AuthResponse(token, User.UserResponse.fromUser(createdUser)))
                }
            }
        }
    }
  }

  // Verify token and get user
  def verifyToken(token: String): Future[Either[String, User]] = {
    jwtUtil.getUserIdFromToken(token) match {
      case Some(userId) =>
        userRepository.findById(userId).map {
          case Some(user) => Right(user)
          case None => Left("User not found")
        }
      case None =>
        Future.successful(Left("Invalid token"))
    }
  }
}