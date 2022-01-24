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

import spire.math.Number


package object functions {

  val registry = IntFunctions.funcs ++ StringFunctions.funcs

  object FunctionsOps {

    implicit def toNum(a:Any):Number = a.toNumber

    implicit class AnyOps(a:Any) {

      def toByte = a.toNumber.toByte
      def toShort = a.toNumber.toShort
      def toInt = a.toNumber.intValue
      def toLong = a.toNumber.longValue
      def toFloat = a.toNumber.toFloat
      def toDouble = a.toNumber.toDouble

      def toNumber = a match {
        case n:Number => n
        case b:Byte => Number(b)
        case s:Short => Number(s)
        case i:Int => Number(i)
        case l:Long => Number(l)
        case d:Double => Number(d)
        case f:Float => Number(f)
        case _=> throw new ClassCastException(s"$a not a Number")
      }

    }
  }

}
