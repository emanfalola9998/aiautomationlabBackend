// app/utils/JwtUtil.scala
package utils

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.Configuration
import play.api.libs.json._
import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.util.{Success, Failure, Try}

@Singleton
class JwtUtil @Inject()(config: Configuration) {

  private val secretKey = config.get[String]("jwt.secret")
  private val algorithm = JwtAlgorithm.HS256
  private val expirationTime = config.get[Int]("jwt.expirationHours") // in hours

  def generateToken(userId: UUID, email: String, name: String): String = {
    val claim = JwtClaim(
      content = Json.obj(
        "userId" -> userId.toString,
        "email" -> email,
        "author" -> name
      ).toString(),
      expiration = Some(Instant.now.plusSeconds(expirationTime * 3600).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )

    Jwt.encode(claim, secretKey, algorithm)
  }

  def validateToken(token: String): Try[JwtClaim] = {
    Jwt.decode(token, secretKey, Seq(algorithm))
  }

  def getUserIdFromToken(token: String): Option[UUID] = {
    validateToken(token) match {
      case Success(claim) =>
        val json = Json.parse(claim.content)
        (json \ "userId").asOpt[String].flatMap  { id =>
          Try(UUID.fromString(id)).toOption
        }
      case Failure(_) => None
    }
  }

  def getAuthorFromToken(token: String): Option[String] = {
    validateToken(token) match {
      case Success(claim) =>
        val json = Json.parse(claim.content)
        (json \ "author").asOpt[String]
      case Failure(_) => None
    }
  }

  def getEmailFromToken(token: String): Option[String] = {
    validateToken(token) match {
      case Success(claim) =>
        val json = Json.parse(claim.content)
        (json \ "email").asOpt[String]
      case Failure(_) => None
    }
  }
}