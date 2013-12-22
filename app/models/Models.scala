package models

import _root_.java.security.MessageDigest
import play.api.Play
import org.joda.time.DateTime
import models.mongoContext.context
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoURI
import java.net.URL
import com.novus.salat.dao._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import play.api.Play.current
import securesocial.core._
import org.apache.commons.codec.binary.Hex

case class Author(
                  id: ObjectId = new ObjectId,
                  username: String,
                  identityId: IdentityId,
                  firstName: String,
                  lastName: String,
                  fullName: String,
                  email: Option[String],
                  avatarUrl: Option[String],
                  authMethod: AuthenticationMethod,
                  oAuth1Info: Option[OAuth1Info],
                  oAuth2Info: Option[OAuth2Info],
                  passwordInfo: Option[PasswordInfo]
) extends Identity {
  def gravatar = {
    val md = MessageDigest.getInstance("MD5")
    val digest = email.map(_.toLowerCase).map(_.getBytes).map(md.digest).map(Hex.encodeHex)
    digest.getOrElse("000000000000000")
  }
}

object Author extends ModelCompanion[Author, ObjectId] {
  def findByIdentity(id: IdentityId): Option[Identity] = dao.findOne(MongoDBObject("identityId.userId" -> id.userId))
  def findByEmail(email: String): Option[Identity] = dao.findOne(MongoDBObject("email" -> email))

  def findOneByUsername(username: String) = Future {
    dao.findOne(MongoDBObject("username" -> username))
  }

  val dao = new SalatDAO[Author, ObjectId](collection = DB.mongoCollection("authors")) {}

  def findOneByName(name: String) = Future {
    dao.findOne(MongoDBObject("name" -> name))
  }
}

case class Category(
                     id: ObjectId = new ObjectId,
                     name: String,
                     path: String)

object Category extends ModelCompanion[Category, ObjectId] {
  def findByPath(path: String) = Future {
    dao.findOne(MongoDBObject("path" -> path))
  }

  val dao = new SalatDAO[Category, ObjectId](collection = DB.mongoCollection("categories")) {}

}

case class Post(
                 id: ObjectId = new ObjectId,
                 title: String,
                 author_id: ObjectId,
                 category_id: ObjectId,
                 created: DateTime = DateTime.now,
                 text: Option[String] = None,
                 link: Option[String] = None,
                 path: String = "",
                 votes_up: Long = 0,
                 votes_down: Long = 0) {

  def author = Author.findOneById(author_id).get

  def category = Category.findOneById(category_id).get

  def commentCount = Post.dao.comments.countByParentId(id)

  def comments = Post.dao.comments.findByParentId(id).$orderby(MongoDBObject("created" -> -1))

  def votes = votes_up - votes_down

}

object Post extends ModelCompanion[Post, ObjectId] {
  val dao = new SalatDAO[Post, ObjectId](collection = DB.mongoCollection("posts")) {
    val comments = new ChildCollection[Comment, Int](collection = DB.mongoCollection("comments"),
      parentIdField = "post_id") {}
  }

  def findByCategory(category_id: ObjectId): Future[SalatMongoCursor[Post]] = Future {
    dao.find(MongoDBObject("category_id" -> category_id)).$orderby(MongoDBObject("created" -> -1)).limit(10)
  }
}

case class PostTextForm(title: String, text: String)
case class PostLinkForm(title: String, link: String)

case class Comment(
                    id: ObjectId = new ObjectId,
                    post_id: ObjectId,
                    author_id: ObjectId,
                    text: String,
                    created: DateTime = DateTime.now,
                    votes_up: Long = 0,
                    votes_down: Long = 0) {
  def author = Author.findOneById(author_id).get
  def votes = votes_up - votes_down
}

object Comment extends ModelCompanion[Comment, ObjectId] {
  val dao = new SalatDAO[Comment, ObjectId](collection = DB.mongoCollection("comments")) {}
}


object DB {
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

  def mongoCollection(collection: String) = database.apply(collection)
}