/*
 * Copyright  2019 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import DependenciesUtils._
import sbt.Keys.libraryDependencies
import sbt._

//scalastyle:off

object Dependencies {

  def profiles(profs: Seq[ModuleID]*): Seq[ModuleID] = {
    profs.flatten.distinct
  }

  def modules(mods: ModuleID*): Seq[ModuleID] = {
    mods.toSeq
  }

  def runtime(mods: ModuleID*): Seq[ModuleID] = {
    mods.map(x=> x % Runtime).toSeq
  }

  //scalastyle:off

  object scalalib {
    lazy val v = "org.scala-lang" ~% BuildSettings.scalaLangVersion
    lazy val reflect = "scala-reflect" ~~ v
    lazy val compiler = "scala-compiler" ~~ v
  }

  object slf4j {
    lazy val v = "org.slf4j" ~% "2.0.9"
    lazy val api = "slf4j-api" ~~ v
  }

  object logback {
    lazy val v = "ch.qos.logback" ~% "1.4.11"
    lazy val classic = "logback-classic" ~~ v
  }

  object akka {
    lazy val v = "com.typesafe.akka" ~%% "2.8.5"

    lazy val actors                 = v ~~ "akka-actor"
    lazy val actorsTyped            = v ~~ "akka-actor-typed"
    lazy val actorsTypedTestKit     = v ~~ "akka-actor-testkit-typed"
    lazy val testKit                = v ~~ "akka-testkit"
    lazy val stream                 = v ~~ "akka-stream"
    lazy val streamTestKit          = v ~~ "akka-stream-testkit"
  }

  object akkaHttp {
    lazy val v          = "com.typesafe.akka" ~%% "10.2.4"
    lazy val http       = v ~~ "akka-http"
    lazy val testKit    = v ~~ "akka-http-testkit"
    lazy val sprayJson  = v ~~ "akka-http-spray-json"
  }

  object scalaTest {
    lazy val v = "org.scalatest" ~%% "3.2.17"
    lazy val scalaTest = v ~~ "scalatest"
  }

  object scalaLog {
    lazy val v = "com.typesafe.scala-logging" ~%% "3.9.5"
    lazy val logging = v ~~ "scala-logging"
  }

  object scalaMock {
    val v = "5.2.0"
    val p = "org.scalamock" ~%% v
    val scalamock = "scalamock" ~~ p
  }

  object jUnit {
    lazy val v = "4.13.2"
    lazy val p = "junit" ~% v
    lazy val jUnit = "junit" ~~ p
  }

  object scoverage {
    lazy val v = "2.0.11"
    lazy val p = "org.scoverage" ~%% v
    lazy val scalacRuntime = "scalac-scoverage-runtime" ~~ p
  }

  object univocity {
    val v = "2.9.1"
    val p = "com.univocity" ~% v
    val parsers = "univocity-parsers" ~~ p
  }

  object jansi {
    val v = "2.4.0"
    val p = "org.fusesource.jansi" ~% v
    val jansi = "jansi" ~~ p
  }

  object ts_config {
    val v = "1.4.2"
    val p = "com.typesafe" ~% v
    val config = "config" ~~ p
  }

  object iheart {
    val v = "1.4.7"
    val p = "com.iheart" ~%% v
    val ficus = "ficus" ~~ p
  }

  object json4s {
    val v = "4.0.6"
    val p = "org.json4s" ~%% v
    val native = "json4s-native" ~~ p
    val jackson = "json4s-jackson" ~~ p
    val ext = "json4s-ext" ~~ p
    val core = "json4s-core" ~~ p
    val ast = "json4s-ast" ~~ p
  }

  object apachePoi {
    val v = "5.2.3"
    val p = "org.apache.poi" ~% v
    val poi = "poi" ~~ p
    val ooxml = "poi-ooxml" ~~ p
  }

  object apacheAvro {
    val v = "1.11.2"
    val p = "org.apache.avro" ~% v
    val avro = "avro" ~~ p
    val avroMapred = "avro-mapred" ~~ p
  }

  object apacheParquet {
    val v = "1.13.1"
    val p = "org.apache.parquet" ~% v
    val parquetAvro = "parquet-avro" ~~ p
  }

  object apacheHadoop {
    val v = "3.3.6"
    val p = "org.apache.hadoop" ~% v
    val client = "hadoop-client" ~~ p
    val common = "hadoop-common" ~~ p
    val aws    = "hadoop-aws" ~~ p
  }

  object amazonAWSSDK {
    val v = "2.20.153"
    val g = "software.amazon.awssdk"
    val p = g ~% v
    val sdkJava = "aws-sdk-java" ~~ p
    val s3 = "s3" ~~ p
  }

  object apacheSpark {
    val v = "3.2.1"
    val p = "org.apache.spark" ~%% v
    val sql = "spark-sql" ~~ p
    val core = "spark-core" ~~ p
  }

  object h2db {
    val v = "2.2.224"
    val p = "com.h2database" ~% v
    val h2 = "h2" ~~ p
  }

  object minio {
    val v = "8.5.6"
    val p = "io.minio" ~% v
    val minio = "minio" ~~ p
  }

  object orientdb {
    val v = "3.2.4"
    val p = "com.orientechnologies" ~% v
    val graphdb = "orientdb-graphdb" ~~ p
  }

  object commons {
    val io = "commons-io" % "commons-io" % "2.13.0"
    val lang3 = "org.apache.commons" % "commons-lang3" % "3.13.0"
  }

  object apacheCalcite {
    val v = "1.35.0"
    val p = "org.apache.calcite" ~% v
    val core = "calcite-core" ~~ p
  }

  object cats {
    val v = "2.1.0"
    val p = "org.typelevel" ~%% v
    val core = "cats-core" ~~ p
  }

  object spire {
    val v = "0.18.0"
    val p = "org.typelevel" ~%% v
    val core = "spire" ~~ p
  }

  object picocli {
    val v = "4.7.5"
    val p = "info.picocli" ~% v
    val picocli = "picocli" ~~ p
    val jline3shell = "picocli-shell-jline3" ~~ p
  }

  object apacheSparl {
    val v = "3.5.1"
    val p = "org.apache.spark" ~%% v
    val core = "spark-core" ~~ p
    val sql  = "spark-sql" ~~ p
  }

  /*
  "org.jline" % "jline" % "3.19.0",
      "org.jline" % "jline-builtins" % "3.19.0",
      "org.jline" % "jline-terminal-jansi" % "3.21.0",
      "org.jline" % "jline-terminal-jna" % "3.21.0",
      "org.jline" % "jline-reader" % "3.21.0",
      "org.jline" % "jline-console" % "3.21.0",
      "org.jline" % "jline-remote-ssh" % "3.21.0",
      "org.jline" % "jline-remote-telnet" % "3.21.0",
      "org.jline" % "jline-style" % "3.21.0",
      "org.jline" % "jline-groovy" % "3.21.0",
      "info.picocli" % "picocli-shell-jline3" % "4.6.2"
   */

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.10"

  implicit class ProjectProfiles(p:Project) {

    lazy val json4sCommon: Seq[ModuleID] = Seq(
      json4s.ast,
      json4s.core,
      json4s.ext)

    lazy val json4sNative: Seq[ModuleID] = json4sCommon ++ Seq(
      json4s.native)

    lazy val json4sJackson: Seq[ModuleID] = json4sCommon ++ Seq(
      json4s.jackson)

    def withConfig : Project = {
      p.settings(
        libraryDependencies ++= Seq(
          Dependencies.ts_config.config
        )
      )
    }

    def withJson : Project = {
      p.settings(
        libraryDependencies ++= json4sJackson
      )
    }
  }

  object DepProfiles {

    lazy val lib: Seq[ModuleID] = Seq(
      slf4j.api,
      logback.classic,
      scalaLog.logging ,
      jUnit.jUnit % Test,
      scalaTest.scalaTest % Test,
      scalaMock.scalamock % Test,
      "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % Test,
      scoverage.scalacRuntime % Test
    )


  }

  //scalastyle:on
}
