import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._
import Dependencies.DepProfiles._

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

lazy val `flow-text` = libProject("flow","flow-text")
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      univocity.parsers
    ) ++ json4sJackson
  )

lazy val `flow-aws` = libProject("flow","flow-aws")
  .withConfig
  .withIntegration
  .dependsOn(`flow-core`)
  .settings(
    libraryDependencies ++= modules(
      amazonAWSSDK.s3,
      minio.minio % IntegrationTest
    ) ++ json4sJackson
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
  .settings(
    libraryDependencies ++= modules(
      orientdb.graphdb
    ) ++ json4sJackson
  )
