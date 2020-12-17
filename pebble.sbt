import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._


lazy val `pebble` = project
  .aggregate(
    `pebble-api`
  )

lazy val `pebble-api` = libProject("pebble","pebble-api")