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
 *  limitations under the License.
 */

package io.qpointz.flow.ql

package object types {

  trait QAny[+T] {
    val value: T
  }

  trait QAnyVal[+T <: AnyVal] extends QAny[T]

  trait QNumeric[+T <: AnyVal] extends QAnyVal[T]

  case class QBoolean(value: Boolean) extends QAnyVal[Boolean]

  case class QString(value: String) extends QAny[String]

  case class QLong(value: Long) extends QNumeric[Long]

  case class QInt(value: Int) extends QNumeric[Int]

  case class QDouble(value: Double) extends QNumeric[Double]

  case class QFloat(value: Float) extends QNumeric[Float]

  case class QChar(value: Char) extends QAnyVal[Char]

  case class QByte(value: Byte) extends QNumeric[Byte]

  case class QShort(value: Short) extends QNumeric[Short]

  case class QObject(value: Any) extends QAny[Any]

  object QAny {
    def apply(v: Any): QAny[_] = v match {
      case b: Boolean => QBoolean(b)
      case s: String => QString(s)
      case i: Int => QInt(i)
      case l: Long => QLong(l)
      case f: Float => QFloat(f)
      case d: Double => QDouble(d)
      case s:Short => QShort(s)
      case ch:Char => QChar(ch)
      case by:Byte => QByte(by)
      case av => QObject(av)
    }

    implicit class QAnyMethods(q: QAny[_]) {

      private def isType(pf: PartialFunction[QAny[_], Boolean]): Boolean = pf.applyOrElse[QAny[_],Boolean](q, _=>false)
      private def asTypeOpt[T](pf: PartialFunction[QAny[_], T]): Option[T] = pf.lift(q)
      private def asTypeOr[T](f:()=>Option[T], or:T):T = f() match {
        case None => or
        case Some(x) => x
      }

      def isNumeric: Boolean = isType { case _: QNumeric[_] => true }

      def isString: Boolean = isType {
        case QString(_) => true
        case QChar(_) => true
      }

      def isBoolean: Boolean = isType { case QBoolean(_) => true }

      def isObject: Boolean = isType { case QObject(_) => true }

      def asStringOpt: Option[String] = asTypeOpt {
        case x: QString => x.value
        case c: QChar => c.value.toString
      }
      def asStringOr(or:String):String = asTypeOr(()=>asStringOpt, or)

      def asBooleanOpt: Option[Boolean] = asTypeOpt { case x: QBoolean => x.value }
      def asBooleanOr(or:Boolean):Boolean = asTypeOr(()=>asBooleanOpt, or)

      def asIntOpt: Option[Int] = asTypeOpt {
        case QInt(x) => x
        case QLong(l) => l.toInt
        case QFloat(f) => f.toInt
        case QDouble(d) => d.toInt
        case QChar(c) => c.toInt
        case QByte(b) => b.toInt
        case QShort(s) => s.toInt
      }
      def asIntOr(or:Int):Int = asTypeOr(()=>asIntOpt, or)

      def asLongOpt:Option[Long] = asTypeOpt {
        case QInt(x) => x.toLong
        case QLong(l) => l
        case QFloat(f) => f.toLong
        case QDouble(d) => d.toLong
        case QChar(c) => c.toLong
        case QByte(b) => b.toLong
        case QShort(s) => s.toLong
      }
      def asLongOr(or:Long):Long = asTypeOr(()=>asLongOpt, or)

      def asFloatOpt:Option[Float] = asTypeOpt {
        case QInt(x) => x.toFloat
        case QLong(l) => l.toFloat
        case QFloat(f) => f
        case QDouble(d) => d.toFloat
        case QChar(c) => c.toFloat
        case QByte(b) => b.toFloat
        case QShort(s) => s.toFloat
      }
      def asFloatOr(or:Float):Float = asTypeOr(()=>asFloatOpt, or)

      def asDoubleOpt:Option[Double] = asTypeOpt {
        case QInt(x) => x.toDouble
        case QLong(l) => l.toDouble
        case QFloat(f) => f.toDouble
        case QDouble(d) => d.toDouble
        case QChar(c) => c.toDouble
        case QByte(b) => b.toDouble
        case QShort(s) => s.toDouble
      }
      def asDoubleOr(or:Double):Double = asTypeOr(()=>asDoubleOpt, or)

      def asShortOpt:Option[Short] = asTypeOpt {
        case QInt(x) => x.toShort
        case QLong(l) => l.toShort
        case QFloat(f) => f.toShort
        case QDouble(d) => d.toShort
        case QChar(c) => c.toShort
        case QByte(b) => b.toShort
        case QShort(s) => s
      }
      def asShortOr(or:Short):Short = asTypeOr(()=>asShortOpt, or)

      def asByteOpt:Option[Byte] = asTypeOpt {
        case QInt(x) => x.toByte
        case QLong(l) => l.toByte
        case QFloat(f) => f.toByte
        case QDouble(d) => d.toByte
        case QChar(c) => c.toByte
        case QByte(b) => b
        case QShort(s) => s.toByte
      }
      def asShortOr(or:Byte):Byte = asTypeOr(()=>asByteOpt, or)
    }
  }
}
