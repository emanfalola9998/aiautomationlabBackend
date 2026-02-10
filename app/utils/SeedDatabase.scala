package utils

import models.{Blog, Comments}
import repository.{BlogTable, CommentsTable}
import utils.MyPostgresProfile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

object SeedDatabase {

  private val db = Database.forConfig("slick.dbs.default.db")

  private val blogs = TableQuery[BlogTable]
  private val comments = TableQuery[CommentsTable]

  // -------------------------------------------------------
  // 1) Blog Dummy Data
  // -------------------------------------------------------
  val blogData: Seq[Blog] = Seq(
    Blog("blg-001", "How AI Is Transforming Small Businesses",
      "Artificial intelligence is helping small companies automate repetitive tasks and make smarter decisions...",
      Some("https://picsum.photos/800/400?1"), "Emmanuel", 34,
      Some("ai"), Instant.parse("2025-01-15T10:00:00Z")),

    Blog("blg-002", "Top 5 Tools for Automating Your Workflow",
      "Automation tools are becoming essential for entrepreneurs looking to scale without hiring large teams...",
      Some("https://picsum.photos/800/400?2"), "Admin", 18,
      Some("automation"), Instant.parse("2025-01-18T14:20:00Z")),

    Blog("blg-003", "Why Machine Learning Matters in 2025",
      "Machine learning continues to dominate the tech landscape as new models emerge...",
      Some("https://picsum.photos/800/400?3"), "Emmanuel", 56,
      Some("future"), Instant.parse("2025-02-01T09:00:00Z")),

    Blog("blg-004", "The Rise of Low-Code AI Platforms",
      "Low-code AI is democratizing access to machine learning capabilities for everyday users...",
      Some("https://picsum.photos/800/400?4"), "Sarah", 22,
      Some("ai"), Instant.parse("2025-02-05T13:30:00Z")),

    Blog("blg-005", "Automation Mistakes to Avoid in Your Startup",
      "Many startups rush into automation without establishing proper structure or processes...",
      Some("https://picsum.photos/800/400?5"), "Emmanuel", 12,
      Some("automation"), Instant.parse("2025-02-10T17:10:00Z")),

    Blog("blg-006", "How Chatbots Are Changing Customer Service",
      "Chatbots powered by large language models are reshaping how companies handle customer interactions...",
      Some("https://picsum.photos/800/400?6"), "Admin", 41,
      Some("ai"), Instant.parse("2025-02-14T11:05:00Z")),

    Blog("blg-007", "The Benefits of AI-Powered Marketing Automation",
      "AI-driven marketing automation can significantly increase customer engagement...",
      Some("https://picsum.photos/800/400?7"), "Sarah", 63,
      Some("automation"), Instant.parse("2025-03-01T08:45:00Z")),

    Blog("blg-008", "Improving Productivity with Task Automation",
      "Task automation allows businesses to streamline their operations and reduce time spent on manual tasks...",
      Some("https://picsum.photos/800/400?8"), "Emmanuel", 27,
      Some("productivity"), Instant.parse("2025-03-10T15:00:00Z")),

    Blog("blg-009", "Understanding LLMs: A Beginner's Guide",
      "Large language models are powerful AI systems designed to understand and generate human-like text...",
      Some("https://picsum.photos/800/400?9"), "Admin", 77,
      Some("ai"), Instant.parse("2025-03-20T12:00:00Z")),

    Blog("blg-010", "AI Trends Dominating 2025",
      "From generative AI to autonomous agents, 2025 is shaping up to be a groundbreaking year...",
      Some("https://picsum.photos/800/400?10"), "Sarah", 89,
      Some("future"), Instant.parse("2025-03-25T16:00:00Z"))
  )

  // -------------------------------------------------------
  // 2) Comment Dummy Data
  // -------------------------------------------------------
  val commentData: Seq[Comments] = Seq(
    Comments("blg-001", 1, "John", "Super helpful article!", Instant.parse("2025-01-15T11:00:00Z"), 5),
    Comments("blg-001", 2, "Mia", "Loved the examples.", Instant.parse("2025-01-15T12:30:00Z"), 4),
    Comments("blg-001", 3, "Alex", "Very insightful.", Instant.parse("2025-01-15T13:10:00Z"), 5),

    Comments("blg-002", 1, "Mary", "These tools are game changers!", Instant.parse("2025-01-18T15:00:00Z"), 5),
    Comments("blg-002", 2, "Kai", "Which tool is best for CRM?", Instant.parse("2025-01-18T16:20:00Z"), 4),
    Comments("blg-002", 3, "David", "Very helpful!", Instant.parse("2025-01-18T17:45:00Z"), 4),

    Comments("blg-003", 1, "Sarah", "This clarified a lot.", Instant.parse("2025-02-01T10:15:00Z"), 5),
    Comments("blg-003", 2, "Chris", "ML is the future!", Instant.parse("2025-02-01T11:40:00Z"), 5),
    Comments("blg-003", 3, "Emily", "Great overview.", Instant.parse("2025-02-01T12:10:00Z"), 4),

    Comments("blg-004", 1, "Sam", "Low-code FTW.", Instant.parse("2025-02-05T14:10:00Z"), 4),
    Comments("blg-004", 2, "Henry", "Exciting tech!", Instant.parse("2025-02-05T15:50:00Z"), 5),
    Comments("blg-004", 3, "Tina", "Loved this write-up.", Instant.parse("2025-02-05T16:30:00Z"), 5),

    Comments("blg-005", 1, "Leo", "Learned a lot!", Instant.parse("2025-02-10T18:00:00Z"), 4),
    Comments("blg-005", 2, "Jack", "Good advice.", Instant.parse("2025-02-10T18:45:00Z"), 4),
    Comments("blg-005", 3, "Ava", "Thanks for sharing!", Instant.parse("2025-02-10T19:15:00Z"), 5),

    Comments("blg-006", 1, "Grace", "Chatbots are evolving fast!", Instant.parse("2025-02-14T12:10:00Z"), 5),
    Comments("blg-006", 2, "Omar", "Very interesting read.", Instant.parse("2025-02-14T13:30:00Z"), 4),
    Comments("blg-006", 3, "Derek", "Good insights.", Instant.parse("2025-02-14T14:50:00Z"), 4),

    Comments("blg-007", 1, "Amy", "Marketing automation is powerful!", Instant.parse("2025-03-01T09:30:00Z"), 5),
    Comments("blg-007", 2, "Isaac", "Great explanation.", Instant.parse("2025-03-01T10:20:00Z"), 4),
    Comments("blg-007", 3, "Zoe", "Very useful!", Instant.parse("2025-03-01T11:05:00Z"), 5),

    Comments("blg-008", 1, "Paul", "Love productivity hacks.", Instant.parse("2025-03-10T16:10:00Z"), 4),
    Comments("blg-008", 2, "Kate", "Nice article!", Instant.parse("2025-03-10T16:40:00Z"), 5),
    Comments("blg-008", 3, "Ivan", "Helpful tips.", Instant.parse("2025-03-10T17:20:00Z"), 4),

    Comments("blg-009", 1, "Sasha", "LLMs are amazing!", Instant.parse("2025-03-20T13:00:00Z"), 5),
    Comments("blg-009", 2, "Oliver", "Great intro.", Instant.parse("2025-03-20T13:45:00Z"), 4),
    Comments("blg-009", 3, "Ella", "Very clear writing.", Instant.parse("2025-03-20T14:15:00Z"), 5),

    Comments("blg-010", 1, "Jason", "Amazing trends!", Instant.parse("2025-03-25T17:00:00Z"), 5),
    Comments("blg-010", 2, "Ruby", "Love these predictions.", Instant.parse("2025-03-25T17:40:00Z"), 4),
    Comments("blg-010", 3, "Hannah", "Super exciting future.", Instant.parse("2025-03-25T18:10:00Z"), 5)
  )

  // -------------------------------------------------------
  // 3) Run Insert
  // -------------------------------------------------------
  def run(): Unit = {

    val action = for {
      _ <- blogs.delete
      _ <- comments.delete
      _ <- blogs ++= blogData
      _ <- comments ++= commentData
    } yield ()

    val future = db.run(action)

    future.onComplete {
      case Success(_) => println("🌱 Successfully seeded blogs + comments!")
      case Failure(e) => println("❌ Seeding failed: " + e.getMessage)
    }

    Await.result(future, 20.seconds)
  }
}
