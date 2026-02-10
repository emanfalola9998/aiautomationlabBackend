package repository

import models.{Blog, Comments}
import javax.inject.Inject
import scala.concurrent.{Future, ExecutionContext}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import utils.MyPostgresProfile.api._

class BlogRepository @Inject() (
                                 protected val dbConfigProvider: DatabaseConfigProvider
                               )(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db
  private val blogs = TableQuery[BlogTable]



  def getAll: Future[Seq[Blog]] =
    db.run(blogs.sortBy(_.datePublished.desc).result)

  def getById(id: String): Future[Option[Blog]] =
    db.run(blogs.filter(_.id === id).result.headOption)

  def create(blog: Blog): Future[Int] =
    db.run(blogs += blog)

  def update(id: String, blog: Blog): Future[Int] =
    db.run(blogs.filter(_.id === id).update(blog))

  def delete(id: String): Future[Int] =
    db.run(blogs.filter(_.id === id).delete)

  def getBlogWithComments(blogId: String): Future[(Option[Blog], Seq[Comments])] = {
    val blogF = db.run(blogs.filter(_.id === blogId).result.headOption)
    val commentsF = db.run(TableQuery[CommentsTable].filter(_.blogId === blogId).result)

    for {
      blog <- blogF
      comments <- commentsF
    } yield (blog, comments)
  }
}
