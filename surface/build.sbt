organization in ThisBuild := "io.qpointz"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.0"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.7" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.2.2" % Test

lazy val `surface` = (project in file("."))
  .aggregate(`surface-api`,
             `surface-impl`,
             `surface-stream-api`,
             `surface-stream-impl`,
             `organization-api`,
             `organization-impl`
  )

lazy val `organization-api` = (project in file("organization-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `organization-impl` = (project in file("organization-impl"))
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



lazy val `surface-api` = (project in file("surface-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `surface-impl` = (project in file("surface-impl"))
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

lazy val `surface-stream-api` = (project in file("surface-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `surface-stream-impl` = (project in file("surface-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`surface-stream-api`, `surface-api`)
