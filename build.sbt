import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._

name := "qpointz"
organization in ThisBuild := "io.qpointz"
version in ThisBuild := BuildSettings.version
scalaVersion in ThisBuild := BuildSettings.scalaLangVersion
cancelable in Global := true

lazy val `flow` = project

lazy val `qpointz` = project.in(file("."))
  .aggregate(
    `flow`,
    `surface-api`,
    `surface-impl`,
    `surface-stream-api`,
    `surface-stream-impl`,
    `organization-api`,
    `organization-impl`
  )


val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `organization-api` = (project in file("surface/organization-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `organization-impl` = (project in file("surface/organization-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`organization-api`)



lazy val `surface-api` = (project in file("surface/surface-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `surface-impl` = (project in file("surface/surface-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`surface-api`)

lazy val `surface-stream-api` = (project in file("surface/surface-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `surface-stream-impl` = (project in file("surface/surface-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`surface-stream-api`, `surface-api`)

resolvers += Resolver.sonatypeRepo("snapshots")
