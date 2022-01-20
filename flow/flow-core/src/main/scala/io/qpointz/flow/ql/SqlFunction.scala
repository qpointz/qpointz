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

import io.qpointz.flow.ql.functions.registry
import org.apache.calcite.sql.SqlBasicCall

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

sealed trait FunctionCall extends QlValueExpression
case class FunctionCallMapped1(fn:List[Any]=>Any, args:Seq[QlValueExpression]) extends FunctionCall
case class FunctionCallDecl1(name:String, args:Seq[QlValueExpression]) extends FunctionCall
object FunctionCall {

  def apply(fn:List[Any]=>Any, args:Seq[QlValueExpression]) : FunctionCall = {
    FunctionCallMapped1(fn, args)
  }

  def apply(name:String, args:Seq[QlValueExpression]) : FunctionCall = {
    FunctionCallDecl1(name, args)
  }

  private def mapByName(str: String):Seq[Any] => Try[Any] = {
    val f = registry(str)
    (x:Seq[Any]) => try {
          Success(f(x))
        } catch {
          case ex: Exception => Failure(ex)
        }
    }

  def map(fce:FunctionCall):FunctionCallMapped1 = fce match {
    case m: FunctionCallMapped1 => m
    case FunctionCallDecl1(name, args) => FunctionCallMapped1(mapByName(name), args)
  }
}


object QlSqlFunction {
  import io.qpointz.flow.ql.Sql._

  def apply(f: SqlBasicCall): QlExpression = {
    FunctionCall(f.getOperator.getName, f.getOperandList.asScala.map(valueExpression).toList)
  }

}
