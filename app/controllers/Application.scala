package controllers

import play.api.mvc._
import models._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object Application extends Controller with securesocial.core.SecureSocial {

  def index = UserAwareAction.async { implicit request =>
    for {
      posts <- Post.findByScore()
    } yield Ok(views.html.index(posts.toIterator))
  }

  def category(category: String) = UserAwareAction.async { implicit request =>
    val catQuery = Category.findByPath(category)
    for {
        catOpt <- catQuery
        cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
        posts <- Post.findByCategory(cat.id)
    } yield Ok(views.html.category(cat, posts.toIterator))
  }

}