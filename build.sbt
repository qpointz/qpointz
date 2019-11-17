import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._

name := "flow"
organization in ThisBuild := "io.qpointz"
version in ThisBuild := BuildSettings.version
scalaVersion in ThisBuild := BuildSettings.scalaLangVersion
cancelable in Global := true

lazy val `flow` = project.in(file("."))
  .aggregate(
    `flow-core`,
    `flow-excel`,
  )

lazy val `flow-core` = project.in(file("flow-core"))
  .settings(
    name := "flow-core",
    libraryDependencies ++= profiles(
      DepProfiles.lib
    )
  )

lazy val `flow-excel` = project.in(file("flow-excel"))
  .dependsOn(`flow-core`)
  .settings(
    name := "flow-excel",
    libraryDependencies ++= profiles(
      DepProfiles.lib
    ) ,
    libraryDependencies ++= modules(
      apachePoi.ooxml,
      apachePoi.poi
    )
  )

resolvers += Resolver.sonatypeRepo("snapshots")
