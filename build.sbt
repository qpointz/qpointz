import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._

name := "qpointz"
organization in ThisBuild := "io.qpointz"
version in ThisBuild := BuildSettings.version
scalaVersion in ThisBuild := BuildSettings.scalaLangVersion
cancelable in Global := true

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

lazy val `flow` = project.in(file("flow"))
  .aggregate(
    `flow-core`,
    `flow-excel`,
    `flow-jdbc`,
    `flow-parquet`
  )

lazy val `flow-core` = libProject("flow", "flow-core")
lazy val `flow-excel` = libProject("flow", "flow-excel")
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      apachePoi.ooxml,
      apachePoi.poi
    )
  )

lazy val `flow-jdbc` = libProject("flow" ,"flow-jdbc")
  .dependsOn(`flow-core`)
  .settings (
    libraryDependencies ++= modules(
      "com.h2database" % "h2" % "1.4.200"
    )
  )

  lazy val `flow-parquet` = libProject("flow", "flow-parquet")
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
