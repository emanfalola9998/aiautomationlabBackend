package utils

import repository.BlogTable
import models.Blog
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import utils.MyPostgresProfile.api._
import scala.util.{Success, Failure}
import java.time.Instant

object DatabaseTest extends App {

  // Database from application.conf
  val db = Database.forConfig("slick.dbs.default.db")

  // TableQuery for BlogTable
  val blogs = TableQuery[BlogTable]

  // Sample blog to insert
  val sampleBlog = Blog(
    id = "1",
    title = "Hello World",
    content = "This is a test blog",
    image = None,
    author = "Admin",
    likes = 0,
    tags = None,
    datePublished = Instant.now()
  )

  // Insert blog into database
  val insertAction = db.run(blogs += sampleBlog)

  // Wait for completion and print result
  insertAction.onComplete {
    case Success(_) => println("✅ Test blog inserted successfully!")
    case Failure(e) => println(s"❌ Failed to insert test blog: ${e.getMessage}")
  }

  // Block to make sure the program doesn't exit immediately
  Await.result(insertAction, 10.seconds)
}
