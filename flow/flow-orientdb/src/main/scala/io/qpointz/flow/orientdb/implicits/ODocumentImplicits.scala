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
import org.json4s.JObject
import org.json4s.JsonAST._


class ODocumentImplicits(e:ODocument) {

  import io.qpointz.flow.orientdb._

  def asJValue:JValue = {
    def loop(value: Any):JValue = value match {
      case null => JNull
      case dc: ODocument => dc.asJValue
      case arr: Iterable[_] => JArray(arr.map(loop(_)).toList)
      case s:String => JString(s)
      case b:Boolean => JBool(b)
      case i: Int => JInt(i)
      case bi: BigInt => JInt(bi)
      case d:Double => JDouble(d)
      case dc:BigDecimal => JDecimal(dc)
      case l:Long => JLong(l)
      // $COVERAGE-OFF$
      case x => throw new RuntimeException(s"Type ${x.getClass} not supported")
      // $COVERAGE-ON$
    }

    val fields = e.fieldNames().map(x=> (x, loop(e.field[Any](x)))).toList
    JObject(fields)
  }
}
