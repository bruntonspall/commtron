package controllers

import play.api._
import play.api.mvc._
import models.{Category, Author, Post}
import org.joda.time.DateTime

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Index Page", Post.findAll().toList))
  }

  def setup = Action {
    Ok("Ok")
  }
}