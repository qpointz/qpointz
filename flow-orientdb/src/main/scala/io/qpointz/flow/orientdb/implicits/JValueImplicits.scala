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

package io.qpointz.flow.orientdb.implicits

import com.orientechnologies.orient.core.record.impl.ODocument
import org.json4s.{JArray, JBool, JNumber, JObject, JString, JsonAST}
import org.json4s.JsonAST.{JNothing, JNull, JValue}

class JValueImplicits(jn: JValue) {

  import io.qpointz.flow.orientdb._

  def asODocument: ODocument = {

    def loop(jv: JValue): Option[Any] = jv match {
      case x:JNumber => Some(x.values)
      case JString(s) => Some(s)
      case JBool(b) => Some(b)
      case jo:JObject => Some(jo.asODocument)
      case JArray(vals) => {
        val a = vals.map(loop(_)).filter(_.isDefined).map(_.get).toArray
        Some(a)
      }
      case JNothing => None

      case JNull => Some[Any](null) // scalastyle:ignore null
      // $COVERAGE-OFF$
      case _ => throw new RuntimeException(s"${jv.toString} not supported")
      // $COVERAGE-ON$
    }

    jn match {
      case jo: JObject => jo.obj
        .foldLeft(new ODocument())((x, y) => loop(y._2) match {
          case None => x
          case Some(v) => x.field(y._1, v)
        })
      case x => throw new IllegalArgumentException(s" ${x} can't be represented as document")
    }
  }
}