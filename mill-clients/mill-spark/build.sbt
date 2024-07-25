import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._
import sbt.Keys.streams


import sbt._

name := "mill-spark"
ThisBuild / organization := "io.qpointz"
ThisBuild / version := BuildSettings.version
ThisBuild / scalaVersion := BuildSettings.scalaLangVersion
Global / cancelable := true
ThisBuild / parallelExecution := false
ThisBuild / versionScheme := Some("pvp")
ThisBuild / evictionErrorLevel := Level.Warn

logLevel:= Level.Debug

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value
)

libraryDependencies ++= modules(
  apacheSpark.core,
  apacheSpark.sql,
)
//libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.11"
libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
"com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)


/*
ThisBuild / publishTo := {
  val nexus = "https://nexus.qpointz.io"
  if (isSnapshot.value) {
    Some( ("snapshots" at nexus + "/repository/maven-snapshots/"))
  } else {
    Some( ("releases" at nexus + "/repository/maven-releases/"))
  }
}
*/

/*
val nexusUser = sys.env.get("NEXUS_USER")
val nexusPassword = sys.env.get("NEXUS_PASSWORD")
if (nexusUser.nonEmpty && nexusPassword.nonEmpty) {
  println("Use nexus credentials from env vars")
  credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.qpointz.io", nexusUser.get , nexusPassword.get)
} else {
  println("Use nexus credentials from ~/.ivy2/.credentials")
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
}
*/

ThisBuild / publishMavenStyle := true



/* temporaly disabled
ThisBuild / coverageFailOnMinimum := true
ThisBuild / coverageMinimumStmtTotal := 90
ThisBuild / coverageMinimumBranchTotal := 90
ThisBuild / coverageMinimumStmtPerPackage := 90
ThisBuild / coverageMinimumBranchPerPackage := 85
ThisBuild / coverageMinimumStmtPerFile := 85
ThisBuild / coverageMinimumBranchPerFile := 80
 */



//lazy val `flow-cli` = libProject("mill","spark")
//  .dependsOn(
//    `flow-core`,
//    `flow-excel`,
//    `flow-text`,
//    `flow-jdbc`,
//    `flow-avro-parquet`,
//    `flow-aws`,
//    `flow-stream`,
//    `flow-workflow`,
//    `shape-core`)
//  .withConfig
//  .withJson
//  .settings(
//    libraryDependencies ++= modules(
//      apacheCalcite.core,
//      jansi.jansi,
//      shapeless,
//      picocli.picocli,
//      picocli.jline3shell,
//      "de.vandermeer" % "asciitable" % "0.3.2",
//      "org.jline" % "jline" % "3.23.0" ,
//      "org.jline" % "jline-builtins" % "3.23.0",
//      "org.jline" % "jline-terminal-jansi" % "3.23.0" % Runtime,
//      //"org.jline" % "jline-terminal-jna" % "3.18.0"% Runtime,
//      //"org.jline" % "jline-reader" % "3.18.0"% Runtime,
//      //"org.jline" % "jline-console" % "3.18.0"% Runtime,
//      //"org.jline" % "jline-remote-ssh" % "3.18.0"% Runtime,
//      //"org.jline" % "jline-remote-telnet" % "3.18.0"% Runtime,
//      //"org.jline" % "jline-style" % "3.18.0"% Runtime,
//      //"org.jline" % "jline-groovy" % "3.18.0"% Runtime,
//    ),
//    Compile / mainClass  := Some("io.qpointz.flow.cli.CliMain"),
//    Compile / discoveredMainClasses := Seq(),
//    executableScriptName := "flow",
//
//  )
//  .enablePlugins(JavaAppPackaging)

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
resolvers += "QP Nexus snapshots" at "https://nexus.qpointz.io/repository/maven-snapshots"
resolvers += "QP Nexus releases" at "https://nexus.qpointz.io/repository/maven-releases"

