package controllers

import play.api.mvc._
import models.{Category, Post}
import org.bson.types.ObjectId
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

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
}