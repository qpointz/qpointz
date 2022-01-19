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

import io.qpointz.flow.ql.QlFunction

object ArgMethods {

  implicit class ArgListMethods(args:Seq[Any]) {
    private lazy val argl = args.lift

    def asStringOp(idx:Int):Option[String] = argl(idx) match {
      case Some(s:String) => Some(s)
      case Some(x:Any) => Some(x.toString)
      case _ => None
    }

    def asString(idx:Int):String = asStringOp(idx) match {
      case Some(s)=> s
      case None => throw new RuntimeException("Index doesn't exists or can't be casted to String")
    }

    def asIntOp(idx:Int):Option[Int] = argl(idx) match {
        case None => None
        case Some(a:Byte) => Some(a.toInt)
        case Some(s:Short) => Some(s.toInt)
        case Some(i:Int) => Some(i)
        case Some(l:Long) => Some(l.toInt)
        case Some(f:Float) => Some(f.toInt)
        case Some(d:Double) => Some(d.toInt)
        case _ => None
      }

      def asInt(idx:Int):Int = asIntOp(idx) match {
        case Some(i) => i
        case None => throw new RuntimeException("Index doesn't exists or can't be casted to Int")
      }

  }

}

object StringFunctions {
  import QlFunction._
  import ArgMethods._

  val str = func(
    {a:Any => a.toString},
    {l=> l})

  val concat = funcimpl(
    {x:Seq[String]=> x.mkString },
    {args:Seq[Any] => args.map(_.toString)})((f,a)=> f(a)) _

  val format = func[String, Seq[Any], String](
        {(x,y)=> x.format(y:_*)},
        {a=> (a.head.toString, a.tail)}
      )

  val replace = func[String, String, String, String](
    {(in, oldstr, newstr) => in.replace(oldstr, newstr)},
    {a => (a(0).toString, a(1).toString, a(2).toString)}
  )

  val substr = func[String, Int, Option[Int],String](
    {(s, st, en)=> en match {
        case None => s.substring(st)
        case Some(end) => s.substring(st, end)
    }},
    {a => (a.asString(0), a.asInt(1), a.asIntOp(2))}
  )

  val regexMatches = func[String, String, Boolean](
    {(p,m)=> p.r.matches(m)},
    {a=> (a(0).toString, a(1).toString)}
  )

  lazy val funcs = Map[String, QlFunction.QFuncImpl](
    "STR" -> str,
    "CONCAT" -> concat,
    "FORMAT" -> format,
    "REPLACE" -> replace,
    "SUBSTR" -> substr,
    "IS_MATCHES_RX" -> regexMatches
  )

}