import sbt._  

name := "qpointz"
ThisBuild / organization := "io.qpointz"
ThisBuild / version := BuildSettings.version
ThisBuild / scalaVersion := BuildSettings.scalaLangVersion
Global / cancelable := true
ThisBuild / parallelExecution := false

lazy val `flow` = project
lazy val `lakehouse` = project

lazy val `qpointz` = project.in(file("."))
  .aggregate(
    `flow`,
    `lakehouse`,
  )

resolvers += Resolver.sonatypeRepo("snapshots")
