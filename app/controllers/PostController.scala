package controllers

import play.api.mvc.Controller
import models._
import org.bson.types.ObjectId
import scala.concurrent.Future
import play.api.data.Form
import play.api.data.Forms._
import models.PostTextForm
import models.PostLinkForm
import scala.concurrent.ExecutionContext.Implicits.global


object PostController extends Controller with securesocial.core.SecureSocial {
  val postTextForm = Form(mapping("title" -> nonEmptyText, "text" -> nonEmptyText)(PostTextForm.apply)(PostTextForm.unapply))
  val postLinkForm = Form(mapping("title" -> nonEmptyText, "link" -> nonEmptyText)(PostLinkForm.apply)(PostLinkForm.unapply))

  def post(category: String, post: String) = UserAwareAction.async { implicit request =>
    val catQuery = Category.findByPath(category)
    for {
      catOpt <- catQuery
      cat <- catOpt.map(Future.successful).getOrElse(Future.failed(new Exception))
      post <- Post.findOneById(new ObjectId(post)).map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield Ok(views.html.post(cat, post, user=request.user.map { case a:Author => a}))
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
        case "up" => post.increment_votes()
        case "down" => post.decrement_votes()
      }

      Ok
    }

  }

}
