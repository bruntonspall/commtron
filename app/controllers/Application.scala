package controllers

import play.api._
import play.api.mvc._
import models.{Category, Author, Post}
import org.joda.time.DateTime
import models.mongoContext.context
import org.bson.types.ObjectId

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Index Page", Post.findAll()))
  }

  def category(category: String) = Action {
    Category.findByPath(category).map {
        cat => Ok(views.html.category("Category Page", cat, Post.findByCategory(cat.id)))
      }.getOrElse(NotFound)
  }

  def post(category: String, post: String) = Action {
    Category.findByPath(category).map {
      cat => Post.findOneById(new ObjectId(post)).map {
        post => Ok(views.html.post("Category Page", cat, post))
      }.getOrElse(NotFound)
    }.getOrElse(NotFound)
  }
}