import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._


lazy val `pebble` = project
  .aggregate(
    `pebble-core`,
    `pebble-api`
  )

lazy val `pebble-core` = libProject("pebble","pebble-core")

lazy val `pebble-api` = libProject("pebble","pebble-api")
  .dependsOn(`pebble-core`)