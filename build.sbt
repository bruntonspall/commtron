name := "commtron"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  "org.mongodb" %% "casbah" % "2.6.3",
  "com.novus" %% "salat" % "1.9.4",
  "se.radley" %% "play-plugins-salat" % "1.4.0",
  "securesocial" %% "securesocial" % "2.1.2"
)

play.Project.playScalaSettings

play.Project.routesImport += "se.radley.plugin.salat.Binders._"

play.Project.templatesImport += "org.bson.types.ObjectId"

resolvers += Resolver.url("sbt-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)