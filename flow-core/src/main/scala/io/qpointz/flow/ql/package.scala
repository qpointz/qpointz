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

package io.qpointz.flow

import scala.util.Try

package object ql {

  sealed trait QlExpression

  trait QlValueExpression extends QlExpression
  case class Attribute(key:AttributeKey) extends QlValueExpression
  case class MetadataEntry(group:String,key:String) extends QlValueExpression
  case class Constant(value:Any) extends QlValueExpression

  trait ProjectionElement extends QlExpression
  object Asterisk extends ProjectionElement {}
  case class ProjectionValue(ex:QlValueExpression, alias:Option[AttributeKey]=None) extends ProjectionElement
  case class Projection(exp:Seq[ProjectionElement]) extends QlExpression

  trait FromExpression extends QlExpression
  case class FromIdentified(names:List[String]) extends FromExpression

  case class QlQuery(select:Projection, from:Option[FromExpression])

  type QlFunctionNResult[_] = Try[_]

}
