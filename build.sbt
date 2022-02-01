import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._


import sbt._  

name := "qpointz"
ThisBuild / organization := "io.qpointz"
ThisBuild / version := BuildSettings.version
ThisBuild / scalaVersion := BuildSettings.scalaLangVersion
Global / cancelable := true
ThisBuild / parallelExecution := false


lazy val `qpointz` = project.in(file("."))
  .aggregate(
    `flow-core`,
    `flow-excel`,
    `flow-text`,
    `flow-jdbc`,
    `flow-avro-parquet`,
    `flow-cli`,
    `flow-aws`,
    `flow-stream`,
    `flow-workflow`,
    `flow-orientdb`,
    `lakehouse-cli`
  )
/* temporaly disabled
ThisBuild / coverageFailOnMinimum := true
ThisBuild / coverageMinimumStmtTotal := 90
ThisBuild / coverageMinimumBranchTotal := 90
ThisBuild / coverageMinimumStmtPerPackage := 90
ThisBuild / coverageMinimumBranchPerPackage := 85
ThisBuild / coverageMinimumStmtPerFile := 85
ThisBuild / coverageMinimumBranchPerFile := 80
 */

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val `flow-cli` = libProject("flow","cli")
  .dependsOn(`flow-core`,
    `flow-core`,
    `flow-excel`,
    `flow-text`,
    `flow-jdbc`,
    `flow-avro-parquet`,
    `flow-aws`,
    `flow-stream`,
    `flow-workflow`,
    `flow-orientdb`,
  )
  .withConfig
  .withJson
  .settings(
    libraryDependencies ++= modules(
      apacheCalcite.core,
      jansi.jansi,
      shapeless,
      picocli.picocli,
      picocli.jline3shell,
      "de.vandermeer" % "asciitable" % "0.3.2",
      "org.jline" % "jline" % "3.18.0" ,
      "org.jline" % "jline-builtins" % "3.18.0",
      "org.jline" % "jline-terminal-jansi" % "3.18.0"% Runtime,
      //"org.jline" % "jline-terminal-jna" % "3.18.0"% Runtime,
      //"org.jline" % "jline-reader" % "3.18.0"% Runtime,
      //"org.jline" % "jline-console" % "3.18.0"% Runtime,
      //"org.jline" % "jline-remote-ssh" % "3.18.0"% Runtime,
      //"org.jline" % "jline-remote-telnet" % "3.18.0"% Runtime,
      //"org.jline" % "jline-style" % "3.18.0"% Runtime,
      //"org.jline" % "jline-groovy" % "3.18.0"% Runtime,
    ),
    Compile / mainClass  := Some("io.qpointz.flow.cli.CliMain"),
    Compile / discoveredMainClasses := Seq(),
    executableScriptName := "flow",

  )
  .enablePlugins(JavaAppPackaging)

lazy val `flow-core` = libProject("flow","core")
  .withConfig
  .withJson
  .settings(
    libraryDependencies ++= modules(
      scalalib.reflect,
      apacheCalcite.core,
      commons.lang3,
      commons.io,
      spire.core,
      shapeless
    )
  )


lazy val `flow-excel` = libProject("flow","excel")
  .dependsOn(`flow-core`)
  .settings(
    Compile / mainClass := Some("io.qpointz.flow.cli.ResTest"),
    libraryDependencies ++= modules(
      apachePoi.ooxml,
      apachePoi.poi
    )

  )

lazy val `flow-jdbc` = libProject("flow","jdbc")
  .dependsOn(`flow-core`)
  .settings (
    libraryDependencies ++= modules(
      h2db.h2
    )
  )

lazy val `flow-avro-parquet` = libProject("flow","avro-parquet")
  .dependsOn(`flow-core`)
  .withJson
  .settings(
    libraryDependencies ++= modules(
      apacheParquet.parquetAvro,
      apacheHadoop.common,
      apacheHadoop.client,
      apacheAvro.avro,
      apacheAvro.avroMapred
    )
  )

lazy val `flow-text` = libProject("flow","text")
  .dependsOn(`flow-core`)
  .withJson
  .settings(
    libraryDependencies ++= modules(
      univocity.parsers
    )
  )

lazy val `flow-aws` = libProject("flow","aws")
  .withConfig
  .withIntegration
  .withJson
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      amazonAWSSDK.s3,
      minio.minio % IntegrationTest
    )
  )


lazy val `flow-stream` = libProject("flow","stream")
  .dependsOn(`flow-core`)
  .dependsOn(`flow-text` % "test->compile")
  .settings(
    libraryDependencies ++= modules(
      akka.stream,
      akka.testKit % Test,
      akka.streamTestKit % Test
    )
  )
  .withIntegration

lazy val `flow-workflow` = libProject("flow","workflow")
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      akka.actorsTyped,
      akka.actorsTypedTestKit % Test
    )
  )

lazy val `flow-orientdb` = libProject("flow", "orientdb")
  .dependsOn(`flow-core`)
  .withConfig
  .withIntegration
  .withJson
  .settings(
    libraryDependencies ++= modules(
      orientdb.graphdb,
      commons.io
    )
  )

lazy val `lakehouse-cli` = libProjectNoDependencies("lakehouse","cli")
  .settings(
    libraryDependencies ++= modules(
      apacheSpark.sql,
      apacheSpark.core,
      apacheHadoop.client,
      apacheHadoop.common,
      apacheHadoop.aws,
      minio.minio,
      "io.netty" % "netty-transport-native-epoll" % "4.1.72.Final" % Provided//,
      //"io.delta" %% "delta-core" % "1.0.0"
    ),
    libraryDependencies += "com.google.guava" % "guava" % "31.0.1-jre" % Provided,
    libraryDependencies += amazonAWSSDK.sdkJava % Provided,
    libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.1"

  )