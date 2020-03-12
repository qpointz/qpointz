import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._

name := "flow"
organization in ThisBuild := "io.qpointz"
version in ThisBuild := BuildSettings.version
scalaVersion in ThisBuild := BuildSettings.scalaLangVersion
cancelable in Global := true

lazy val `flow` = project.in(file("."))
  .aggregate(
    `flow-core`,
    `flow-excel`,
    `flow-jdbc`,
    `flow-parquet`
  )

lazy val `flow-core` = libProject("flow-core")

lazy val `flow-excel` = libProject("flow-excel")
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      apachePoi.ooxml,
      apachePoi.poi
    )
  )

lazy val `flow-jdbc` = libProject("flow-jdbc")
  .dependsOn(`flow-core`)
  .settings(

  )

  lazy val `flow-parquet` = libProject("flow-parquet")
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      "org.apache.parquet" % "parquet-avro" % "1.11.0",
      "org.apache.hadoop" % "hadoop-client" % "3.2.1",
      "org.apache.hadoop" % "hadoop-common" % "3.2.1",
      "org.apache.avro" % "avro" % "1.9.2",
      "org.apache.avro" % "avro-mapred" % "1.9.2",
      "software.amazon.awssdk" % "aws-sdk-java" % "2.10.85",
      scala.reflect
    )
  )

resolvers += Resolver.sonatypeRepo("snapshots")
