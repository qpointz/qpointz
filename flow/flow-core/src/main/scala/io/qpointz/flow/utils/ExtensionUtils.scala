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

package io.qpointz.flow.utils

import com.typesafe.config.{ConfigFactory, ConfigValueType}
import com.typesafe.scalalogging.Logger

import java.net.URL
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.{Failure, Try}

object ExtensionUtils {

  private val log = Logger(classOf[ExtensionUtils.type])

  private lazy val extinstances = {
    val cl = this.getClass.getClassLoader
    cl
      .getResources("qpointz.conf")
      .asIterator()
      .asScala
      .flatMap (k=>{
        val cfg = ConfigFactory.parseURL(k)
        cfg
          .getList("extensions")
          .iterator()
          .asScala
          .map {
            case x if x.valueType() == ConfigValueType.STRING => Some((k,x.unwrapped().asInstanceOf[String]))
            case y => {
              log.warn("Extension {} ignored. Class name should be string", y)
              None
            }
          }
          .filter(_.isDefined)
          .map(_.get)
          .toSeq
      })
      .map(l=>reflect.tryNewInstanceByName[Any](l._2) match {
        case Failure(ex1) => {
          log.error(s"Can't load extension ${l._2} from ${l._1}" , ex1)
          Failure(ex1) }
        case succ => succ
       })
      .filter(_.isSuccess)
      .map(_.get)
      .toSet
  }

  def extensionsOf[T](implicit m:Manifest[T]):Seq[T] = reflect.instancesOf[T](extinstances)
}
