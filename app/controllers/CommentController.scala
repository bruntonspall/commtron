package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import models.{Comment, Author, Post, PostCommentForm}
import scala.concurrent.Future
import play.api.Logger
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import models.PostCommentForm
import scala.concurrent.ExecutionContext.Implicits.global


object CommentController  extends Controller with securesocial.core.SecureSocial {

  val postCommentForm = Form(mapping("text" -> nonEmptyText, "parent" -> optional(text))(PostCommentForm.apply)(PostCommentForm.unapply))

  def postComment(comment: String, post: String) = SecuredAction.async { implicit request =>
    for {
      post <- Post.findOneById(new ObjectId(post)).map(Future.successful).getOrElse(Future.failed(new Exception))
    } yield {
      postCommentForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.error("Form has errors: "+formWithErrors)
          BadRequest(views.html.post(post.category, post))
        },
        textForm => {
          request.user match {
            case user: Author => {
              Comment.save(Comment(author_id = user.id, post_id = post.id, text=textForm.text))
              Redirect("/c/"+post.category.path+"/"+post.id)
            }
            case _ => BadRequest(views.html.post(post.category, post))
          }
        }
      )
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
