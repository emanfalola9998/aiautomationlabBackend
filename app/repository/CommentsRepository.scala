package repository

import javax.inject.{Inject, Singleton}
import models.Comments
import utils.MyPostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

@Singleton
class CommentsRepository @Inject()(
                                    protected val dbConfigProvider: DatabaseConfigProvider
                                  )(implicit ec: ExecutionContext) {

  private val db = dbConfigProvider.get[JdbcProfile].db
  private val comments = TableQuery[CommentsTable]


  def createComment(comment: Comments): Future[Comments] = {
    val insertQuery = (comments returning comments.map(_.id) into ((comment, id) => comment.copy(id = id))) += comment
    db.run(insertQuery)
  }

  def getForBlog(blogId: String): Future[Seq[Comments]] =
    db.run(comments.filter(_.blogId === blogId).result)

  def getOne(blogId: String, id: Int): Future[Option[Comments]] =
    db.run(comments.filter(c => c.blogId === blogId && c.id === id).result.headOption)

  def delete(blogId: String, id: Int): Future[Int] =
    db.run(comments.filter(c => c.blogId === blogId && c.id === id).delete)

  def update(comment: Comments): Future[Int] = {
    db.run(comments.filter(_.id === comment.id).update(comment))
  }
}
