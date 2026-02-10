package repository

//import slick.jdbc.PostgresProfile.api._
import models.Blog
import java.time.Instant
import utils.MyPostgresProfile.api._



class BlogTable(tag: Tag) extends Table[Blog](tag, "blogs") {

  def id = column[String]("id", O.PrimaryKey)
  def title = column[String]("title")
  def content = column[String]("content")
  def image = column[Option[String]]("image")
  def author = column[String]("author")
  def likes = column[Int]("likes")
  def tags = column[Option[String]]("tags")
  def datePublished = column[Instant]("date_published")

  def * =
    (id, title, content, image, author, likes, tags, datePublished)
      .<>(Blog.tupled, Blog.unapply)
}
