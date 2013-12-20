/**
 * Created by michaelbruntonspall on 20/12/2013.
 */

import com.mongodb.casbah.Imports._
import org.joda.time.DateTime
import play.api._
import libs.ws.WS
import models._
import se.radley.plugin.salat._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()
    if (Author.count(DBObject(), Nil, Nil) == 0) {
      val mbs = Author.insert(Author(
        name = "Michael Brunton-Spall",
        username = "bruntonspall",
        gravatar = "037360597d7b529eed1e61bb2329abc9")).get
      val barry = Author.insert(Author(
        name = "Barney McNuggin",
        username = "mcnuggin",
        gravatar = "00000000000000000000000000000000")).get

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
