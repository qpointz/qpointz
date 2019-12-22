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
    `flow-parquet`
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

  lazy val `flow-parquet` = project.in(file("flow-parquet"))
  .dependsOn(`flow-core`)
  .settings(
    name := "flow-parquet",
    libraryDependencies ++= profiles(
      DepProfiles.lib
    ) ,
    libraryDependencies ++= modules(
      "org.apache.parquet" % "parquet-avro" % "1.10.1",
      "org.apache.hadoop" % "hadoop-client" % "3.2.0",
      "org.apache.hadoop" % "hadoop-common" % "3.2.0",
      "org.apache.avro" % "avro" % "1.9.0",
      "org.apache.avro" % "avro-mapred" % "1.9.0",
      "software.amazon.awssdk" % "aws-sdk-java" % "2.10.40",
      scala.reflect
    )
  )

resolvers += Resolver.sonatypeRepo("snapshots")
