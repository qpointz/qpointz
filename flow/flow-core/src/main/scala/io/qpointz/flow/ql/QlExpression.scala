/*
 * Copyright 2022 qpointz.io
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

package io.qpointz.flow.ql

import io.qpointz.flow.{AttributeKey, AttributeValue, Attributes, Record}
import scala.util.{Success, Try}

object IteratorMapper {

  def asRecordFunction(ve:QlValueExpression):Record => AttributeValue = {

    def funcCall(funcCall:FunctionCall):Record=>AttributeValue = {
        val fc = FunctionCall.map(funcCall)
        val argFunctions = fc.args
          .map(asRecordFunction(_))
          .toList
        r:Record => {
          val args = argFunctions.map(_(r))
          fc.fn(args)
        }
    }

    ve match {
      case Attribute(k) => r: Record => r.get(k)
      case fc:FunctionCall => funcCall(fc)
      case Constant(v) => _ => v
      case _ => throw new RuntimeException(s"$ve expression not supported on record operations")
    }
  }

  def project(pe:Projection): Record => Record = {

    val projFuncs = pe.exp.zipWithIndex.map(e => e._1 match {
      case ProjectionValue(ex:Attribute, None)  => {
        val fn = asRecordFunction(ex)
        r:Record => Seq[io.qpointz.flow.Attribute](ex.key -> fn(r))
      }
      case ProjectionValue(ve, Some(k))         => {
        val fn = asRecordFunction(ve)
        r:Record => Seq[io.qpointz.flow.Attribute](k -> fn(r))
      }
      case ProjectionValue(ve, None)            => {
        val fn = asRecordFunction(ve)
        val key = s"Col_${e._2}"
        r:Record => Seq[io.qpointz.flow.Attribute](key -> fn(r))
      }
      case Asterisk                             => (r:Record)=>r.attributes
    })

    r:Record => {
      Record(projFuncs.map(_(r)).flatten.toMap, r.meta)
    }
  }

  def apply(q:QlQuery, translate:Boolean=true):Iterator[Record]=> Iterator[Record] = {
    def translateloop(r:Record):Record = {
      val kv = r.items.filter(f=> f match {
        case (_, x:Try[_]) if x.isFailure => false
        case _ => true
      }).map {
        case (k, Success(an)) => (k, an)
        case x => x
      }
      Record(kv.toMap, r.meta)
    }

    val prj: Record => Record = project(q.select)
    iter:Iterator[Record] => {
      val res = iter.map(prj)
      if (translate) {
        res.map(translateloop)
      } else {
        res
      }
    }
  }
}