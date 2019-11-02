import sbt._
import java.nio.file.{Files, Paths}

import sbt.librarymanagement.ModuleID
import sbt.{File, file}

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

import collection.JavaConverters._

object BuildUtils {

  def dirTo(p:File, relTo:String) = {
    val absPath = Paths.get(p.getAbsolutePath)
    Files.walk(absPath).iterator().asScala
      .filter(x=> ! Files.isDirectory(x))
      .map(x=>{
        val relPath = absPath.relativize(x).toString
        val tgtPath = Paths.get(relTo, relPath).toString
        val srcPath = Paths.get(p.getPath, relPath).toString
        file(srcPath) -> tgtPath
      })
      .toSeq
  }
}

object DependenciesUtils {

  trait Dependency {

    def ~~(v:String) = Dependency.from(this, v)

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

    def ~~(dg:Dependency):ModuleID = from(dg)

    def ~%%(v:String) = new ScalaDependency(item, v)
    def ~%(v:String) = new PlainDependency(item, v)

    def %%+(v:String) = new ScalaDependency(v,item)
    def %+(v:String) = new PlainDependency(v, item)


  }
}