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
  def authorId = column[String]("author")
  def authorName = column[String]("author_name")
  def likes = column[Int]("likes")
  def likedBy = column[List[String]]("liked_by")
  def tags = column[Option[String]]("tags")
  def datePublished = column[Instant]("date_published")

  def * =
    (id, title, content, image, authorId, authorName, likes, likedBy, tags, datePublished)
      .<>(
        { case (id, title, content, image, authorId, authorName, likes, likedBy, tags, datePublished) =>
          Blog(id, title, content, image, authorId, authorName, likes, likedBy.toSet, tags, datePublished)
        },
        { blog: Blog =>
          Some((blog.id, blog.title, blog.content, blog.image, blog.authorId, blog.authorName, blog.likes, blog.likedBy.toList, blog.tags, blog.datePublished))
        }
      )
}
