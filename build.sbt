import sbt._  

name := "qpointz"
organization in ThisBuild := "io.qpointz"
version in ThisBuild := BuildSettings.version
scalaVersion in ThisBuild := BuildSettings.scalaLangVersion
cancelable in Global := true

lazy val `flow` = project
lazy val `surface` = project

lazy val `qpointz` = project.in(file("."))
  .aggregate(
    `flow`,
    `surface`
  )


resolvers += Resolver.sonatypeRepo("snapshots")
