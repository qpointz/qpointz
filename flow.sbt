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
    `flow-transform`,
    `flow-fs`
  )

lazy val `flow-core` = libProject("flow","flow-core")

lazy val `flow-excel` = libProject("flow","flow-excel")
  .dependsOn(`flow-core`)
  .settings(
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
  .settings(
    libraryDependencies ++= modules(
      apacheParquet.parquetAvro,
      apacheHadoop.common,
      apacheHadoop.client,
      apacheAvro.avro,
      apacheAvro.avroMapred
    )
  )

lazy val `flow-transform` = libProject("flow","flow-transform")
  .dependsOn(`flow-core`,
             `flow-text` % "it->test",
             `flow-avro-parquet` % "it->test"
  )
  .withIntegration

lazy val `flow-text` = libProject("flow","flow-text")
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      univocity.parsers
    )
  )

lazy val `flow-fs` = libProject("flow", "flow-fs")
  .dependsOn(`flow-core`)
