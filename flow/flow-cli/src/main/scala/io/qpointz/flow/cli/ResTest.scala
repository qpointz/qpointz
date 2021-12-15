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

import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigRenderOptions, ConfigResolveOptions, ConfigResolver, ConfigValue}

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import scala.jdk.CollectionConverters.IteratorHasAsScala



object ResTest {

  def main(args:Array[String]):Unit = {
    val cl = this.getClass.getClassLoader
    val cfg = cl
      .getResources("qpointz.conf")
      .asIterator()
      .asScala
      .map(x=> {
        //println(x)
        //println("==========================================")
        val cfg = x.getContent().asInstanceOf[InputStream]
        //val cnt = scala.io.Source.fromInputStream(cfg).mkString
        //println(cnt)
        //println("==========================================")
        ConfigFactory.parseURL(x)
      })
      .foldLeft(ConfigFactory.load())((l,r) => l.withFallback(r))
      .resolve(new ConfigResolveOptions(new ConfigResolver {
        override def lookup(path: String): ConfigValue = {

        }

        override def withFallback(fallback: ConfigResolver): ConfigResolver = ???
      }))

    val renderOpts = ConfigRenderOptions.defaults().setOriginComments(false).setComments(false).setJson(false)
    println(cfg.root().get("qpointz").render(renderOpts))
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
