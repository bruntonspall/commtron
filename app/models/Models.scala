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
import com.novus.salat.annotations._
import scala.math._
import play.Logger

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

  val votes = votes_up - votes_down

  val sign = if (votes > 0) 1
                      else if (votes < 0) -1
                      else 0
  @Persist val created_secs = created.getMillis / 1000
  @Persist val adj_votes = sign * log10(max(abs(votes), 1))
}

object Post extends ModelCompanion[Post, ObjectId] {
  val scoreQuery = MongoDBObject("$project" ->
    MongoDBObject(
      "_id" -> 1,
      "title" -> 1,
      "author_id" -> 1,
      "category_id" -> 1,
      "created" -> 1,
      "text" -> 1,
      "link" -> 1,
      "path" -> 1,
      "votes_up" -> 1,
      "votes_down" -> 1,
      "created_secs" -> 1,
      "score" -> MongoDBObject("$add" -> List(
        MongoDBObject("$divide" -> List(
          MongoDBObject("$subtract" -> List("$created_secs", DateTime.now().getMillis/1000)),
          43200)),
        "$adj_votes"))
    )
  )
  val dao = new SalatDAO[Post, ObjectId](collection = DB.mongoCollection("posts")) {
    val comments = new ChildCollection[Comment, Int](collection = DB.mongoCollection("comments"),
      parentIdField = "post_id") {}
  }

  def findByScore(): Iterable[Post] = {
    DB.mongoCollection("posts").aggregate(
      scoreQuery,
      MongoDBObject("$sort" -> MongoDBObject("score" -> -1)),
      MongoDBObject("$limit" -> 25)
    ).results.map(dbo => dao._grater.asObject(dbo))
  }


  def findByCategory(category_id: ObjectId): Future[Iterable[Post]] = Future {
    DB.mongoCollection("posts").aggregate(
      MongoDBObject("$match" -> MongoDBObject("category_id" -> category_id)),
      scoreQuery,
      MongoDBObject("$sort" -> MongoDBObject("score" -> -1)),
      MongoDBObject("$limit" -> 25)
    ).results.map(dbo => dao._grater.asObject(dbo))
  }
}

case class PostTextForm(title: String, text: String)
case class PostLinkForm(title: String, link: String)
case class PostCommentForm(text: String, parent: Option[String])

case class Comment(
                    id: ObjectId = new ObjectId,
                    post_id: ObjectId,
                    author_id: ObjectId,
                    text: String,
                    created: DateTime = DateTime.now,
                    votes_up: Long = 1,
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