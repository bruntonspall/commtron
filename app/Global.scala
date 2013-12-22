/**
 * Created by michaelbruntonspall on 20/12/2013.
 */

import com.mongodb.casbah.Imports._
import org.joda.time.DateTime
import play.api._
import libs.ws.WS
import models._
import play.api.Application
import play.mvc.Call
import se.radley.plugin.salat._
import securesocial.core.{AuthenticationMethod, IdentityId}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()
    if (Author.count(DBObject(), Nil, Nil) == 0) {
      val mbs = Author.insert(Author(
        identityId = IdentityId("a", "g"),
        firstName = "Michael",
        lastName = "Brunton-Spall",
        fullName = "Michael Brunton-Spall",
        email = Some("michael@brunton-spall.co.uk"),
        avatarUrl = None,
        oAuth1Info = None,
        oAuth2Info = None,
        passwordInfo = None,
        authMethod = AuthenticationMethod.OAuth2,
        username = "bruntonspall")).get
      val barry = Author.insert(Author(
        identityId = IdentityId("b", "g"),
        firstName = "Barry",
        lastName = "McNuggin",
        fullName = "Barry McNuggin",
        email = Some("barry@brunton-spall.co.uk"),
        avatarUrl = None,
        oAuth1Info = None,
        oAuth2Info = None,
        passwordInfo = None,
        authMethod = AuthenticationMethod.OAuth2,
        username = "mcnuggin")).get

      val foo = Category.insert(Category(name = "Foo", path = "foo")).get
      val bar = Category.insert(Category(name = "Bar", path = "bar")).get
      val baz = Category.insert(Category(name = "Baz", path = "baz")).get

      val p1 = Post.insert(Post(
        title="Some title",
        author_id = mbs,
        created=new DateTime(2013, 10, 31, 19, 31),
        category_id = foo,
        text = None,
        link = None)).get
      val p2 = Post.insert(Post(
        title="Mcnuggin is Awesome!",
        author_id = barry,
        created=new DateTime(2013, 10, 29, 14, 7),
        category_id = bar,
        text = None,
        link = None)).get

      Comment.save(Comment(post_id = p1, author_id=barry, text="This was awesome, thanks"))
      Comment.save(Comment(post_id = p1, author_id=mbs, text="No prlbem"))
      Comment.save(Comment(post_id = p2, author_id=mbs, text="I don't understand, who is awesome?"))
      Comment.save(Comment(post_id = p2, author_id=barry, text="I am"))
      Comment.save(Comment(post_id = p2, author_id=mbs, text="Sure, whatever"))
    }
  }
}
