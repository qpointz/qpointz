import BuildUtils._
import sbt.Keys.libraryDependencies
import sbt._

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.7" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.2.2" % Test

lazy val `surface` = (project in file("."))
  .aggregate(`surface-front`)

lazy val `surface-front` = libProject("surface-front")
  .enablePlugins(PlayScala)
  .settings(
      libraryDependencies += guice,
      libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
  )

//lazy val `surface-api` = libProject()