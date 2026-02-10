// app/repositories/UserRepository.scala
package repositories

import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(
                                protected val dbConfigProvider: DatabaseConfigProvider
                              )(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[UUID]("id", O.PrimaryKey)
    def email = column[String]("email", O.Unique)
    def passwordHash = column[Option[String]]("password_hash")
    def name = column[String]("name")
    def provider = column[String]("provider")
    def providerId = column[Option[String]]("provider_id")
    def createdAt = column[Instant]("created_at")
    def updatedAt = column[Instant]("updated_at")

    def * = (id, email, passwordHash, name, provider, providerId, createdAt, updatedAt) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UsersTable]

  // Create user
  def create(user: User): Future[User] = {
    db.run(users += user).map(_ => user)
  }

  // Find by email
  def findByEmail(email: String): Future[Option[User]] = {
    db.run(users.filter(_.email === email).result.headOption)
  }

  // Find by ID
  def findById(id: UUID): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  // Find by provider ID (for OAuth)
  def findByProviderId(provider: String, providerId: String): Future[Option[User]] = {
    db.run(
      users
        .filter(u => u.provider === provider && u.providerId === providerId)
        .result
        .headOption
    )
  }

  // Update user
  def update(user: User): Future[Int] = {
    db.run(users.filter(_.id === user.id).update(user))
  }

  // Check if email exists
  def emailExists(email: String): Future[Boolean] = {
    db.run(users.filter(_.email === email).exists.result)
  }
}