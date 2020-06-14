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
import sbt._

object Dependencies {

  def profiles(profs: Seq[ModuleID]*):Seq[ModuleID] = {
    profs.flatten.distinct
  }

  def modules(mods: ModuleID*):Seq[ModuleID] = {
    mods.toSeq
  }

//scalastyle:off

  object scala {
    lazy val v        = "org.scala-lang" ~% BuildSettings.scalaLangVersion
    lazy val reflect  = "scala-reflect"  ~~ v
    lazy val compiler = "scala-compiler" ~~ v
  }

  object slf4j {
    lazy val v = "org.slf4j" ~% "1.7.26"
    lazy val api = "slf4j-api" ~~ v
  }

  object logback {
    lazy val v = "ch.qos.logback" ~% "1.2.3"
    lazy val classic = "logback-classic" ~~ v
  }

  object akka {
    lazy val v      = "com.typesafe.akka" ~%% "2.5.23"

    lazy val actors = v ~~ "akka-actor"
    lazy val stream = v ~~ "akka-stream"
  }

  object scalaTest {
    lazy val v  = "org.scalatest" ~%% "3.1.1"
    lazy val scalaTest = v ~~ "scalatest"
  }

  object scalaMock
  {
    val v         = "4.4.0"
    val p         = "org.scalamock" ~%% v
    val scalamock = "scalamock" ~~ p
  }

  object jUnit {
    lazy val v         = "4.13"
    lazy val p         = "junit" ~% v
    lazy val jUnit     = "junit" ~~ p
  }

  object univocity {
    val v       = "2.8.3"
    val p       = "com.univocity" ~% v
    val parsers = "univocity-parsers" ~~ p
  }

  object jansi {
    val v     = "1.18"
    val p     = "org.fusesource.jansi" ~% v
    val jansi = "jansi" ~~ p
  }

  object picocli {
    val v       = "4.0.0"
    val p       = "info.picocli" ~% v
    val picocli = "picocli" ~~ p
  }

  object ts_config {
    val v   = "1.3.4"
    val p   = "com.typesafe" ~% v
    val config = "config" ~~ p
  }

  object iheart {
    val v = "1.4.7"
    val p = "com.iheart" ~%% v
    val ficus = "ficus" ~~ p
  }

  object json4s {
    val v   = "3.6.7"
    val p   = "org.json4s" ~%% v
    val native  = "json4s-native"  ~~ p
    val jackson = "json4s-jackson"  ~~ p
    val ext     = "json4s-ext"  ~~ p
    val core    = "json4s-core"  ~~ p
    val ast     = "json4s-ast"  ~~ p
  }

  object apachePoi {
    val v = "4.1.2"
    val p = "org.apache.poi" ~% v
    val poi = "poi" ~~ p
    val ooxml = "poi-ooxml" ~~ p
  }

  object DepProfiles {
    lazy val lib:Seq[ModuleID] = Seq(
          //  slf4j.api
          //, logback.classic
          //, scalaLog
          //, scalaConfig

           jUnit.jUnit  % Test
          , scalaTest.scalaTest % Test
          //, scalactic
          , scalaMock.scalamock % Test
          , "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test
      )

    lazy val json4sCommon:Seq[ModuleID] = Seq(
      json4s.ast,
      json4s.core,
      json4s.ext
    )

    lazy val json4sNative:Seq[ModuleID] = json4sCommon ++ Seq(
      json4s.native
    )

    lazy val json4sJackson:Seq[ModuleID] = json4sCommon ++ Seq(
      json4s.jackson
    )

  }




  //scalastyle:on
}
