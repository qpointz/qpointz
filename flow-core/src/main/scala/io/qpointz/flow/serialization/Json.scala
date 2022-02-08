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
 *  limitations under the License
 */

package io.qpointz.flow.serialization

import io.qpointz.flow.utils
import org.json4s.{DefaultFormats, Formats, TypeHints}

object Json {

  import io.qpointz.flow.QId._

  def hint[T<:Any](implicit fmt:Formats, m:Manifest[T]):(String, String) = {
    (fmt.typeHints.typeHintFieldNameForClass(m.runtimeClass).get -> fmt.typeHints.hintFor(m.runtimeClass).get)
  }

  lazy val formats : Formats = fromFormats(DefaultFormats.withHints)

  lazy val jsonExtensions: Iterable[JsonProtocolExtension] = utils
    .extension
    .extensionsOf[JsonProtocolExtension]

  lazy val jsonProtocols = jsonExtensions
    .flatMap(x=> x.protocols)
    .toSet

  private def fromFormats(fmt:TypeHints=>Formats) : Formats = {

    val hintPairs = jsonProtocols
      .filter(_.typeId.isDefined)
      .map (x=> (x.typeId.get.jsonTypeHint, x.m.runtimeClass))

    val th = new TypeHints {

      override val hints: List[Class[_]] = jsonProtocols.map(_.m.runtimeClass).toList

      private val h2c = hintPairs.toMap

      private val c2h = hintPairs.map(x=> x._2 -> x._1).toMap

      override def hintFor(clazz: Class[_]): Option[String] = {
        c2h.get(clazz)
      }

      override def classFor(hint: String, parent: Class[_]): Option[Class[_]] = {
        h2c.get(hint)
      }

      override val typeHintFieldName:String = "qp:type"

      override def shouldExtractHints(clazz: Class[_]): Boolean = true
    }

    val f = jsonProtocols
      .filter(_.serializer.isDefined)
      .map(_.serializer.get)
      .foldLeft[Formats](fmt(th))((df, p)=> df + p)
    f
  }
}
