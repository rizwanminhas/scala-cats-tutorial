ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "scala-cats-tutorial"
  )

val Http4sVersion = "1.0.0-M32"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.3.6",
  "org.http4s" % "http4s-blaze-server_3" % Http4sVersion,
  "org.http4s" % "http4s-circe_3" % Http4sVersion,
  "org.http4s" % "http4s-dsl_3" % Http4sVersion,
  "io.circe" %% "circe-generic" % "0.14.1"
)
