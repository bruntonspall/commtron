package models

import play.api.{Logger, Play}
import org.joda.time.DateTime
import models.mongoContext.context
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.dsl._
import com.mongodb.casbah.MongoURI
import java.net.URL
import com.novus.salat.dao.{ModelCompanion, SalatDAO}

case class Author(
                   id: ObjectId = new ObjectId,
                   name: String,
                   username: String,
                   gravatar: String)

object Author extends ModelCompanion[Author, ObjectId] {
  def findOneByUsername(username: String): Option[Author] = dao.findOne(MongoDBObject("username" -> username))

  val dao = new SalatDAO[Author, ObjectId](collection = DB.mongoCollection("authors")) {}

  def findOneByName(name: String): Option[Author] = dao.findOne(MongoDBObject("name" -> name))
}

case class Category(
                    id: ObjectId = new ObjectId,
                    name: String,
                    path: String)

object Category extends ModelCompanion[Category, ObjectId] {
  def findByPath(path: String): Option[Category] = dao.findOne(MongoDBObject("path" -> path))

  val dao = new SalatDAO[Category, ObjectId](collection = DB.mongoCollection("categories")) {}

}

case class Post(
                 id: ObjectId = new ObjectId,
                 title: String,
                 author: Author,
                 created: DateTime,
                 category: Category,
                 text: Option[String],
                 link: Option[URL])

object Post extends ModelCompanion[Post, ObjectId] {
  val dao = new SalatDAO[Post, ObjectId](collection = DB.mongoCollection("posts")) {}

}

case class Comment(
                    id: ObjectId = new ObjectId,
                    author: Author,
                    text: String)

object Comment extends ModelCompanion[Comment, ObjectId] {
  val dao = new SalatDAO[Comment, ObjectId](collection = DB.mongoCollection("comments")) {}
}


object DB {
  import play.api.Play.current
  val mongoUri = MongoURI(Play.configuration.getString("mongodb.default.uri").get)
  lazy val database = mongoUri.connectDB match {
    case Left(thrown) => throw thrown
    case Right(database) =>
      mongoUri.username.map {
        username =>
          mongoUri.password.map {
            password =>
              database.underlying.authenticate(username, password)
          }
      }
      database
  }

  def mongoCollection(collection:String) = database.apply(collection)
}