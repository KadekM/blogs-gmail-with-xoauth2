import Dependencies._

ThisBuild / scalaVersion     := "2.13.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "Gmail with XOAuth2",
    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= Seq(
      "com.google.apis" % "google-api-services-gmail" % "v1-rev110-1.25.0",
      "com.google.api-client" % "google-api-client" % "1.23.0",
      "com.google.oauth-client" % "google-oauth-client-jetty" % "1.23.0",

      "javax.mail" % "mail" % "1.4.7"
    )
  )
