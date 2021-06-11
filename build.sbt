import sbt._  

name := "qpointz"
ThisBuild / organization := "io.qpointz"
ThisBuild / version := BuildSettings.version
ThisBuild / scalaVersion := BuildSettings.scalaLangVersion
Global / cancelable := true
ThisBuild / parallelExecution := false

lazy val `flow` = project
lazy val `surface` = project
lazy val `pebble` = project

lazy val `qpointz` = project.in(file("."))
  .aggregate(
    `flow`,
    `surface`,
    `pebble`
  )

resolvers += Resolver.sonatypeRepo("snapshots")
