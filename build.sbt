name := "commtron"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  "org.mongodb" %% "casbah" % "2.6.3",
  "com.novus" %% "salat" % "1.9.4",
  "se.radley" %% "play-plugins-salat" % "1.4.0"
)     

play.Project.playScalaSettings

play.Project.routesImport += "se.radley.plugin.salat.Binders._"

play.Project.templatesImport += "org.bson.types.ObjectId"