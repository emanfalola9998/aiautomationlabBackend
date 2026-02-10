package repository

import utils.MyPostgresProfile.api._
import models.Comments
import java.time.Instant

// In your CommentsTable definition
class CommentsTable(tag: Tag) extends Table[Comments](tag, "comments") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def blogId = column[String]("blog_id")
  def username = column[String]("username", O.SqlType("VARCHAR"))  // Add this if needed
  def comment = column[String]("comment")
  def timestamp = column[Instant]("timestamp")
  def rating = column[Int]("rating")

  def * = (blogId, id,  username, comment, timestamp, rating) <> ((Comments.apply _).tupled, Comments.unapply)


  // Foreign key → blogs.id
  def blogFk = foreignKey("fk_blog", blogId, TableQuery[BlogTable])(_.id, onDelete=ForeignKeyAction.Cascade)
}
