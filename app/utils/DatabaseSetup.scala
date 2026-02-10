package utils

import repository.{BlogTable, CommentsTable}
import utils.MyPostgresProfile.api._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

object DatabaseSetup {

  private val db = Database.forConfig("slick.dbs.default.db")

  private val blogs = TableQuery[BlogTable]
  private val comments = TableQuery[CommentsTable]

  def createTables(): Unit = {

    val setupAction = DBIO.seq(
      blogs.schema.createIfNotExists,
      comments.schema.createIfNotExists
    )

    val setupFuture = db.run(setupAction)

    setupFuture.onComplete {
      case Success(_) => println("✅ All tables created successfully!")
      case Failure(e) => println(s"❌ Error creating tables: ${e.getMessage}")
    }

    Await.result(setupFuture, 10.seconds)
  }
}
