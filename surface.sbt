import BuildUtils._
import sbt.Keys.libraryDependencies
import sbt._
import Dependencies._

lazy val `surface` = (project in file("surface"))
  .aggregate(
    `surface-api`
  )

lazy val `surface-api` = libProject("surface","surface-api")
  .settings(
    libraryDependencies ++= modules(
      akka.actorsTyped,
      akka.actorsTypedTestKit,
      akka.streamTestKit % Test,

      akkaHttp.http,
      akkaHttp.sprayJson,
      akkaHttp.testKit % Test
    )
  )