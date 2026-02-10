name := """aiAutomateLabBackend"""
organization := "aiAutomateLabBackend"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.18"

resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"


libraryDependencies ++= Seq(
  guice, // dependency injection

  // JSON support
  "com.typesafe.play" %% "play-json" % "2.10.0-RC7",

  // Database
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
  "org.postgresql" % "postgresql" % "42.6.0",

  // JWT
  "com.github.jwt-scala" %% "jwt-play-json" % "9.4.4",
  "com.github.jwt-scala" %% "jwt-core" % "9.4.4",

  // Password hashing
  "org.mindrot" % "jbcrypt" % "0.4",

  // Google OAuth
  "com.google.api-client" % "google-api-client" % "2.4.0",
  "com.google.http-client" % "google-http-client-gson" % "1.43.3",

  // JSON
  "com.typesafe.play" %% "play-json" % "2.10.3",

  // PostgreSQL
  "org.postgresql" % "postgresql" % "42.7.1",


  // Slick ORM
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
  "com.github.tminglei" %% "slick-pg" % "0.21.0",
  "com.github.tminglei" %% "slick-pg_joda-time" % "0.21.0",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.21.0",
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",

)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test

dependencyOverrides ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
  "org.scala-lang.modules" %% "scala-xml" % "2.2.0"
)

PlayKeys.playMonitoredFiles ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "aiAutomateLabBackend.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "aiAutomateLabBackend.binders._"
