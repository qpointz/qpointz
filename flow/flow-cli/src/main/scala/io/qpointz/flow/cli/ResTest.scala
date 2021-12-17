/*
 * Copyright 2021 qpointz.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.qpointz.flow.cli

import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigRenderOptions, ConfigResolveOptions, ConfigResolver, ConfigValue, ConfigValueType}

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import scala.jdk.CollectionConverters.IteratorHasAsScala



object ResTest {

  def main(args:Array[String]):Unit = {
    val cl = this.getClass.getClassLoader
    val all = cl
      .getResources("qpointz.conf")
      .asIterator()
      .asScala
      .map(x=> {
        val cfg = ConfigFactory.parseURL(x)
        cfg
          .getList("extensions")
          .iterator()
          .asScala
          .map({
            case x if x.valueType() == ConfigValueType.STRING => Some(x.unwrapped().asInstanceOf[String])
            case y => None
          })
          .filter(_.isDefined)
          .map(_.get)
          .toSeq
      })
      .flatten
      .toSet


    //val cfg
/*
    val uri = ResTest.getClass.getClassLoader.getResource("/").toURI

    val p = Paths.get(uri)
    Files
      .list(p)
      .map(_.toString)
      .filter(_.contains("conf"))
      .forEach(println)
*/
  }

}
