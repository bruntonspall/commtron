/**
 * Created by michaelbruntonspall on 20/12/2013.
 */

import com.mongodb.casbah.Imports._
import play.api._
import libs.ws.WS
import models._
import se.radley.plugin.salat._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()
    //      if (User.count(DBObject(), Nil, Nil) == 0) {
    //        Logger.info("Loading Testdata")
  }
}
