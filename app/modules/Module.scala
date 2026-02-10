package modules

import com.google.inject.AbstractModule
import repository.CommentsRepository
import utils.MyPostgresProfile.api._

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Database]).toInstance(Database.forConfig("slick.dbs.default.db"))
    bind(classOf[CommentsRepository]).asEagerSingleton()
  }
}
