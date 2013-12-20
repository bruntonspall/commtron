package models

import play.api.Play
import org.joda.time.DateTime
import models.mongoContext.context
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoURI
import java.net.URL
import com.novus.salat.dao._

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
                 author_id: ObjectId,
                 created: DateTime,
                 category_id: ObjectId,
                 text: Option[String],
                 link: Option[URL]) {
  def author = Author.findOneById(author_id).get
  def category = Category.findOneById(category_id).get
  def commentCount = Post.dao.comments.countByParentId(id)
}

object Post extends ModelCompanion[Post, ObjectId] {
  val dao = new SalatDAO[Post, ObjectId](collection = DB.mongoCollection("posts")) {
    val comments = new ChildCollection[Comment, Int](collection = DB.mongoCollection("comments"),
      parentIdField = "post_id") {}
  }

}

case class Comment(
                    id: ObjectId = new ObjectId,
                    post_id: ObjectId,
                    author_id: ObjectId,
                    text: String) {
  def author = Author.findOneById(author_id).get
}

object Comment extends ModelCompanion[Comment, ObjectId] {
  val dao = new SalatDAO[Comment, ObjectId](collection = DB.mongoCollection("comments")) {}
  def countCommentsOnPost(post_id: ObjectId) = 0
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