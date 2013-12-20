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
    Author.save(Author(
      name = "Michael Brunton-Spall",
      username = "bruntonspall",
      gravatar = "037360597d7b529eed1e61bb2329abc9"))
    Author.save(Author(
      name = "Barney McNuggin",
      username = "mcnuggin",
      gravatar = "00000000000000000000000000000000"))
    Category.save(Category(name = "Foo", path = "foo"))
    Category.save(Category(name = "Bar", path = "bar"))
    Category.save(Category(name = "Baz", path = "baz"))
    Post.save(Post(
      title="Some title",
      author = Author.findOneByUsername("bruntonspall").get,
      created=new DateTime(2013, 10, 31, 19, 31),
      category = Category.findByPath("foo").get,
      text = None,
      link = None))
    Post.save(Post(
      title="Mcnuggin is Awesome!",
      author = Author.findOneByUsername("mcnuggin").get,
      created=new DateTime(2013, 10, 29, 14, 7),
      category = Category.findByPath("bar").get,
      text = None,
      link = None))
    Ok("Ok")
  }
}