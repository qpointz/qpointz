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
import io.qpointz.flow.ql.types._

import scala.util.{Success, Try}

sealed trait QlExpression

sealed trait QlValueExpression extends QlExpression
case class Attribute(key:AttributeKey) extends QlValueExpression
case class MetadataEntry(group:String,key:String) extends QlValueExpression
case class Constant(value:Any) extends QlValueExpression

sealed trait FunctionCall extends QlValueExpression
object FunctionCall {

    def apply(fn:List[Any]=>Any, args:Seq[QlValueExpression]) : FunctionCall = {
      FunctionCallMapped(fn, args)
    }

    def apply(name:String, args:Seq[QlValueExpression]) : FunctionCall = {
      FunctionCallDecl(name, args)
    }

    private def mapByName(str: String):Seq[Any] => Try[Any] = IntFunctions.funcs(str)

  def map(fce:FunctionCall):FunctionCallMapped = fce match {
      case m: FunctionCallMapped => m
      case FunctionCallDecl(name, args) => FunctionCallMapped(mapByName(name), args)
    }


}

case class FunctionCallMapped(fn:List[Any]=>Any, args:Seq[QlValueExpression]) extends FunctionCall
case class FunctionCallDecl(name:String, args:Seq[QlValueExpression]) extends FunctionCall

case class ProjectionElement(ex:QlValueExpression, alias:Option[AttributeKey]=None) extends QlExpression
case class Projection(exp:Seq[ProjectionElement]) extends QlExpression

case class QlQuery(select:Projection)

object IteratorMapper {

  def asRecordFunction(ve:QlValueExpression):Record => AttributeValue = {

    def funcCall(funcCall:FunctionCall):Record=>AttributeValue = {

        def argCombine(argfn:Record=>AttributeValue)(rec:Record, args:List[Any]):(Record, List[Any]) = {
          (rec,args :+ argfn(rec))
        }

        val fc = FunctionCall.map(funcCall)

        val aa = fc.args
          .map(asRecordFunction)
          .map(f=> (argCombine(f) _).tupled)
          .reduce((l,r)=>l.andThen(r))

        r:Record => fc.fn(aa(r, List())._2)
    }

    ve match {
      case Attribute(k) => r: Record => r.get(k)
      case fc:FunctionCall => funcCall(fc)
      case Constant(v) => _ => v
      case _ => throw new RuntimeException(s"$ve expression not supported on record operations")
    }
  }

  def project(pe:Projection): Record => Record = {

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
      case ProjectionElement(ex:Attribute, None)  => (ex.key, asRecordFunction(ex))
      case ProjectionElement(ve, Some(k))               => (k, asRecordFunction(ve))
      case ProjectionElement(ve, None)                  => (s"Col_${e._2}", asRecordFunction(ve))
    })

    asProjFunc(projFuncs)
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