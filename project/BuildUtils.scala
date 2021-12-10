/*
 * Copyright 2019 qpointz.io
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

import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import Keys._
import sbt.Def._
import java.nio.file.{Files, Path, Paths}

import sbt.librarymanagement.ModuleID
import sbt.{File, file}

import scala.sys.process.Process

object BuildUtils {

  implicit class QpProject(p:Project) {

    def withIntegration: Project = {
      p.configs(IntegrationTest)
        .settings(
          Defaults.itSettings,
          libraryDependencies ++= Seq(
            jUnit.jUnit % IntegrationTest,
            scalaTest.scalaTest % IntegrationTest,
            scalaMock.scalamock % IntegrationTest,
            "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % IntegrationTest
          )
        )
    }

    def withMinimalCoverage: Project = {

      /* temporaly disabled
ThisBuild / coverageFailOnMinimum := true
ThisBuild / coverageMinimumStmtTotal := 90
ThisBuild / coverageMinimumBranchTotal := 90
ThisBuild / coverageMinimumStmtPerPackage := 90
ThisBuild / coverageMinimumBranchPerPackage := 85
ThisBuild / coverageMinimumStmtPerFile := 85
ThisBuild / coverageMinimumBranchPerFile := 80
 */
    }

    def withConfig : Project = {
        p.settings(
          libraryDependencies ++= Seq(
            Dependencies.ts_config.config
          )
        )
    }

  }

  def projectPath(group:String, projectName:String):String = {
    s"${group}/${projectName}"
  }

  def libProjectNoDependencies(group:String, projectName:String): Project = {
    sbt.Project(projectName, file(projectPath(group, projectName)))
      .settings(
        autoAPIMappings := true,
        name:= projectName
      )
  }

  def libProject(group:String, projectName:String): Project = {
    val pPath = projectPath(group, projectName)
    libProjectNoDependencies(group, projectName)
      .settings(
        libraryDependencies ++= profiles(
          DepProfiles.lib
        ),
        testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oI",
          "-h", s"${pPath}/target/test-reports/html",
          "-u", s"${pPath}/target/test-reports/xml"
        )
      )
  }

}

object DependenciesUtils {

  trait Dependency {

    //noinspection ScalaStyle
    def ~~(v:String):ModuleID = Dependency.from(this, v)

  }

  object  Dependency {

    def from(dg:Dependency, item:String):ModuleID = dg match {
      case PlainDependency(g,v) => g % item % v
      case ScalaDependency(g,v) => g %% item % v
    }

  }

  case class PlainDependency(groupId:String, version:String) extends Dependency

  case class ScalaDependency(groupId:String, version:String) extends Dependency

  implicit class StrExt(item:String) {

    def from(dg:Dependency):ModuleID = Dependency.from(dg, item)

    //noinspection ScalaStyle
    def ~~(dg:Dependency):ModuleID = from(dg)

    //noinspection ScalaStyle
    def ~%%(v:String):ScalaDependency = ScalaDependency(item, v)

    //noinspection ScalaStyle
    def ~%(v:String):PlainDependency = PlainDependency(item, v)

    //noinspection ScalaStyle
    def %%+(v:String):ScalaDependency = ScalaDependency(v, item)

    //noinspection ScalaStyle
    def %+(v:String):PlainDependency = PlainDependency(v, item)
  }
}
