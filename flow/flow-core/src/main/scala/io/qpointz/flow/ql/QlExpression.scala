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
import io.qpointz.flow.MetadataMethods._
import io.qpointz.flow.ql.types.QAny

sealed trait QlExpression

sealed trait QlValueExpression extends QlExpression
case class FunctionCallExpression(fn:List[QAny[_]]=>QAny[_], args:Seq[QlValueExpression]) extends QlValueExpression
case class RecordAttribute(key:AttributeKey) extends QlValueExpression

sealed trait ProjectionElementExpression extends QlExpression
case class ExpressionElement(ex:QlValueExpression) extends ProjectionElementExpression
case class AliasExpressionElement(ex:QlValueExpression, al:AttributeKey) extends ProjectionElementExpression

case class ProjectionExpression(exp:Seq[ProjectionElementExpression]) extends QlExpression
case class QlQuery(select:ProjectionExpression)

object IteratorMapper {

  def asRecordFunction(ve:QlValueExpression):Record => AttributeValue = {

    def funcCall(fc:FunctionCallExpression):Record=>AttributeValue = {
        def argCombine(argfn:Record=>AttributeValue)(rec:Record, args:List[QAny[_]]):(Record, List[QAny[_]]) = {
          (
            rec,
            args :+ QAny(argfn(rec))
          )
        }

        val aa = fc.args
          .map(asRecordFunction)
          .map(f=> (argCombine(f) _).tupled)
          .reduce((l,r)=>l.andThen(r))
        r:Record => {
          val args:List[QAny[_]] = aa(r, List())._2
          fc.fn(args)
        }
    }

    ve match {
      case RecordAttribute(k) => r: Record => r.get(k)
      case fc:FunctionCallExpression => funcCall(fc)
      case _ => throw new RuntimeException(s"$ve expression not supported on record operations")
    }
  }

  def project(pe:ProjectionExpression): Record => Record = {

    def attributeCombine(key:AttributeKey, fn:Record=>AttributeValue)(in:(Record, Attributes)):(Record,Attributes) = {
      (
        in._1,
        in._2 + (key -> fn(in._1))
      )
    }

    def asProjFunc(proj:Seq[(AttributeKey, Record=>AttributeValue)]):Record=>Record = {
      val cf = proj
        .map(x=> attributeCombine(x._1, x._2) _)
        .reduce((l,r)=>l.andThen(r))

      r:Record=> {
        val nr = cf(r, Map())
        Record(nr._2, r.meta)
      }
    }

    val projFuncs = pe.exp.zipWithIndex.map(e => e._1 match {
      case AliasExpressionElement(ve, k)          => (k, asRecordFunction(ve))
      case ExpressionElement(ex:RecordAttribute)  => (ex.key, asRecordFunction(ex))
      case ExpressionElement(ex)                  => (s"Col_${e._2}", asRecordFunction(ex))
    })

    asProjFunc(projFuncs)
  }

  def apply(q:QlQuery):Iterator[Record]=> Iterator[Record] = {
    val prj: Record => Record = project(q.select)
    val res = { iter: Iterator[Record] => iter.map(prj) }
    res
  }

}