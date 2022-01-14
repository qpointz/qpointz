
import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._

lazy val `flow` = project
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
    `flow-orientdb`
  )

lazy val `flow-cli` = libProject("flow","flow-cli")
  .dependsOn(`flow-core`,
    `flow-core`,
    `flow-excel`,
    `flow-text`,
    `flow-jdbc`,
    `flow-avro-parquet`,
    `flow-aws`,
    `flow-stream`,
    `flow-workflow`,
    `flow-orientdb`
  )
  .withConfig
  .settings(
    libraryDependencies ++= modules(
      apacheCalcite.core,
      jansi.jansi
    )
  )
  .enablePlugins(JavaAppPackaging)

lazy val `flow-core` = libProject("flow","flow-core")
  .withConfig
  .withJson
  .settings(
    libraryDependencies ++= modules(
      scalalib.reflect,
      apacheCalcite.core
    )
  )


lazy val `flow-excel` = libProject("flow","flow-excel")
  .dependsOn(`flow-core`)
  .settings(
    Compile / mainClass := Some("io.qpointz.flow.cli.ResTest"),
    libraryDependencies ++= modules(
      apachePoi.ooxml,
      apachePoi.poi
    )

  )

lazy val `flow-jdbc` = libProject("flow","flow-jdbc")
  .dependsOn(`flow-core`)
  .settings (
    libraryDependencies ++= modules(
      h2db.h2
    )
  )

lazy val `flow-avro-parquet` = libProject("flow","flow-avro-parquet")
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

lazy val `flow-text` = libProject("flow","flow-text")
  .dependsOn(`flow-core`)
  .withJson
  .settings(
    libraryDependencies ++= modules(
      univocity.parsers
    )
  )

lazy val `flow-aws` = libProject("flow","flow-aws")
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


lazy val `flow-stream` = libProject("flow","flow-stream")
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

lazy val `flow-workflow` = libProject("flow","flow-workflow")
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      akka.actorsTyped,
      akka.actorsTypedTestKit % Test
    )
  )

lazy val `flow-orientdb` = libProject("flow", "flow-orientdb")
  .dependsOn(`flow-core`)
  .withConfig
  .withIntegration
  .withJson
  .settings(
    libraryDependencies ++= modules(
      orientdb.graphdb,
      commonsio.io
    )
  )