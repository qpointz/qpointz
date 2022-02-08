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
 *  limitations under the License.
 */

package io.qpointz.flow.ql.functions

import io.qpointz.flow.ql.{QlFunction, QlFunction1, QlFunction2}
import spire.math._

object NumberFunctions {
  import FunctionsOps._

  implicit class strOp(s:String) {
    def op(opf: (Number, Number) =>Number) = new QlFunction2[Number, Number, Number] {
      override protected val fn: (Number, Number) => Number = opf
      override def apply(args: Seq[Any]): Number = fn(args(0).toNumber, args(1).toNumber)
      override val sqlName: String = s
    }

    def func(opf: Number =>Number) = new QlFunction1[Number, Number] {
      override protected val fn: (Number) => Number = opf
      override def apply(args: Seq[Any]): Number = fn(args(0).toNumber)
      override val sqlName: String = s
    }

    def parse[T](opf: String=>T) = QlFunction(s,opf)
  }

  val plus    = "+" op (_ + _)
  val minus   = "-" op (_ - _)
  val times   = "*" op (_ * _)
  val divide  = "/" op (_ / _)
  val mod     = "%" op (_ tmod _)
  val min     = "MIN" op (_ min _)
  val max     = "MAX" op (_ max _)
  val pow     = "POW" op (_ pow _)

  val abs     = "ABS"  func (_.abs)
  val sign    = "SIGN" func (_.signum)

  val sqrt    = "POW" func ((x:Number)=>pow(x,0.5))

  val toByte    = "TO_BYTE" func (_.byteValue())
  val parseByte = "PARSE_BYTE" parse(_.toByte)

  val toShort     = "TO_SHORT"  func (_.shortValue())
  val parseShort  = "PARSE_SHORT" parse(_.toShort)

  val toInt   = "TO_INT"    func (_.intValue)
  val parseInt = "PARSE_INT" parse(_.toInt)

  val toLong  = "TO_LONG"   func (_.longValue)
  val parseLong = "PARSE_LONG" parse(_.toLong)

  val toFloat = "TO_FLOAT"  func (_.floatValue)
  val parseFloat = "PARSE_FLOAT" parse(_.toFloat)

  val toDouble= "TO_DOUBLE" func (_.doubleValue)
  val parseDouble = "PARSE_DOUBLE" parse(_.toDouble)
}
