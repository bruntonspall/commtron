package controllers

import play.api.mvc._
import models._
import scala.concurrent.Future
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Index Page", Post.findAll()))
  }

  def category(category: String) = Action.async {
    val catQuery = Category.findByPath(category)
    for {
        catOpt <- catQuery
        cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
        posts <- Post.findByCategory(cat.id)
    } yield Ok(views.html.category("Category Page", cat, posts))
  }

  def post(category: String, post: String) = Action.async {
    val catQuery = Category.findByPath(category)
    for {
      catOpt <- catQuery
      cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
      post <- Post.findOneById(new ObjectId(post)).map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield Ok(views.html.post("", cat, post))
  }

  def postVote(post: String, direction: String) = Action.async { request =>
    for {
      post <- Post.findOneById(new ObjectId(post)).map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield {
      // We need to register one vote per person, but we don't have user authentication yet
      val field = direction match {
        case "up" => "votes_up"
        case "down" => "votes_down"
      }
      Post.update(
        q = MongoDBObject("_id" -> post.id),
        o = $inc (field -> 1),
        upsert = false,
        multi = false)
      Ok
    }

  }
  def commentVote(comment: String, direction: String) = Action.async { request =>
    for {
      comment <- Comment.findOneById(new ObjectId(comment)).map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield {
      // We need to register one vote per person, but we don't have user authentication yet
      val field = direction match {
        case "up" => "votes_up"
        case "down" => "votes_down"
      }
      Comment.update(
        q = MongoDBObject("_id" -> comment.id),
        o = $inc (field -> 1),
        upsert = false,
        multi = false)
      Ok
    }

  }
}