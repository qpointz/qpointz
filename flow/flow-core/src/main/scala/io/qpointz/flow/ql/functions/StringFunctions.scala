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

package io.qpointz.flow.ql.functions

import io.qpointz.flow.ql._
import spire.math.Number

object StringFunctions {

  import FunctionsOps._

  val str = QlFunction[Any,String]("STR", { (a:Any) => a.toString})

  val concat = new QlFunction1[Seq[String],String] {
    override val sqlName: String = "CONCAT"
    override protected val fn: Seq[String] => String = x => x.mkString
    override def apply(args: Seq[Any]): String = fn(args.map(_.toString))
  }

  val format = new QlFunction2[String, Seq[Any], String] {
    override val sqlName: String = "STR_FORMAT"
    override protected val fn: (String, Seq[Any]) => String = (x,y) => x.format(y:_*)
    override def apply(args: Seq[Any]): String = fn(args(0).toString, args.tail)
  }

  val replace = QlFunction("REPLACE", { (in:String, oldstr:String, newstr:String) => in.replace(oldstr, newstr)})

  val substr = new QlFunction3[String, Int, Option[Int], String]{
    override val sqlName: String = "SUBSTR"
    override protected val fn: (String, Int, Option[Int]) => String = (s:String, st:Int, en:Option[Int])=> en match {
      case None => s.substring(st)
      case Some(end) => s.substring(st, end)
    }

    override def apply(args:Seq[Any]) = args.toList match {
      case (s:String) :: st :: Nil => fn(s, st.toNumber.intValue, None)
      case (s:String) :: st :: en :: Nil => fn(s, st.toNumber.intValue, Some(en.toNumber.toInt))
      case _ => throw new IllegalArgumentException(s"Unexpected arguments ${args}")
    }
  }

  val regexMatches = QlFunction("IS_MATCHES_RX", { (p:String, m:String)=> p.r.matches(m)})

  lazy val funcs = Map[String, QlFunction[_]](
    "STR" -> str,
    "CONCAT" -> concat,
    "FORMAT" -> format,
    "REPLACE" -> replace,
    "SUBSTR" -> substr,
    "IS_MATCHES_RX" -> regexMatches
  )
}