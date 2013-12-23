package controllers

import play.api.mvc._
import models._
import scala.concurrent.Future
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import play.api.data._
import play.api.data.Forms._
import securesocial.core.SecureSocial
import java.net.URL


object Application extends Controller with securesocial.core.SecureSocial {

  val postTextForm = Form(mapping("title" -> nonEmptyText, "text" -> nonEmptyText)(PostTextForm.apply)(PostTextForm.unapply))
  val postLinkForm = Form(mapping("title" -> nonEmptyText, "link" -> nonEmptyText)(PostLinkForm.apply)(PostLinkForm.unapply))


  def index = UserAwareAction { implicit request =>
    Logger.info("Logged in as user: "+request.user)
    Ok(views.html.index("Index Page", Post.findByScore().toIterator))
  }

  def category(category: String) = UserAwareAction.async { implicit request =>
    val catQuery = Category.findByPath(category)
    for {
        catOpt <- catQuery
        cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
        posts <- Post.findByCategory(cat.id)
    } yield Ok(views.html.category("Category Page", cat, posts.toIterator))
  }

  def post(category: String, post: String) = UserAwareAction.async { implicit request =>
    val catQuery = Category.findByPath(category)
    for {
      catOpt <- catQuery
      cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
      post <- Post.findOneById(new ObjectId(post)).map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield Ok(views.html.post("", cat, post))
  }

  def addText(category: String) = SecuredAction.async { implicit request =>
    val catQuery = Category.findByPath(category)
    for {
      catOpt <- catQuery
      cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield {
      Ok(views.html.addText(cat, postTextForm))
    }
  }

  def postText(category: String) = SecuredAction.async { implicit request =>
    val catQuery = Category.findByPath(category)
    for {
      catOpt <- catQuery
      cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield {
      postTextForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.addText(cat, formWithErrors))
        },
        textForm => {
          request.user match {
            case user: Author => {
              Post.save(Post(title=textForm.title, author_id = user.id, category_id = cat.id, text = Some(textForm.text)))
              Redirect(routes.Application.index)
            }
            case _ => BadRequest(views.html.addText(cat, postTextForm))
          }
        }
      )
    }
  }

  def addLink(category: String) = SecuredAction.async { implicit request =>
    val catQuery = Category.findByPath(category)
    for {
      catOpt <- catQuery
      cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield {
      Ok(views.html.addLink(cat, postLinkForm))
    }
  }

  def postLink(category: String) = SecuredAction.async { implicit request =>
    val catQuery = Category.findByPath(category)
    for {
      catOpt <- catQuery
      cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield {
      postLinkForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.addLink(cat, formWithErrors))
        },
        textForm => {
          request.user match {
            case user: Author => {
              Post.save(Post(title=textForm.title, author_id = user.id, category_id = cat.id, link = Some(textForm.link)))
              Redirect(routes.Application.index)
            }
            case _ => BadRequest(views.html.addLink(cat, postLinkForm))
          }
        }
      )
    }
  }

  def postVote(post: String, direction: String) = UserAwareAction.async { implicit request =>
    for {
      post <- Post.findOneById(new ObjectId(post)).map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield {
      // We need to register one vote per person, but we don't have user authentication yet
      direction match {
        case "up" => Post.save(post.copy(votes_up = post.votes_up + 1))
        case "down" => Post.save(post.copy(votes_down = post.votes_down + 1))
      }

      Ok
    }

  }
  def commentVote(comment: String, direction: String) = UserAwareAction.async { implicit request =>
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