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
 *  limitations under the License
 */

package io.qpointz.flow.serialization.extclasses

import io.qpointz.flow.serialization.{JsonFormat, _}
import org.json4s.{CustomSerializer, Formats, JObject}
import org.json4s.JsonDSL._
import scala.runtime.ScalaRunTime
import Json._

sealed trait TestCase {
  def a :String
  def b :String
}

case class TestCaseA(a:String, b:String) extends TestCase

class TestCaseB(override val a:String, override val b:String)  extends TestCase {

  override def equals(obj: Any): Boolean = obj match {
    case a:TestCaseB => a == a.a && b == a.b
    case _ => false
  }

  override def hashCode(): Int = ScalaRunTime._hashCode((a , System.identityHashCode(b)))
}

class TestCaseBSerializer extends CustomSerializer[TestCaseB](implicit format =>({
    case jp:JObject =>
      val a = (jp \ "tcb_a").extract[String]
      val b = (jp \ "tcb_b").extract[String]
      new TestCaseB(a, b)
  },{
    case tc:TestCaseB =>
        hint[TestCaseB] ~ ("tcb_a" -> tc.a) ~ ("tcb_b"-> tc.b)
  }))


class TestCaseC(override val a:String, override val b:String) extends TestCase {
  override def equals(obj: Any): Boolean = obj match {
    case a:TestCaseC => a == a.a && b == a.b
    case _ => false
  }

  override def hashCode(): Int = ScalaRunTime._hashCode((a , System.identityHashCode(b)))
}

class TestCaseCSerializer extends CustomSerializer[TestCaseC](implicit format => ({
  case jp:JObject =>
    val a = (jp \ "tcc_a").extract[String]
    val b = (jp \ "tcc_b").extract[String]
    new TestCaseC(a, b)
  },{
  case tc:TestCaseC =>
      hint[TestCaseC] ~
      ("tcc_a" -> tc.a) ~
      ("tcc_b"-> tc.b)
  }))

class ExtExtensions extends JsonFormatExtension {
  override def hintNamespace: String = "qp-test"

  override def protocols: Iterable[JsonFormat[_]] = List(
    JsonFormat[TestCaseA]("message", "test-case-a", None),
    JsonFormat[TestCaseB]("message", "test-case-b", Some(new TestCaseBSerializer())),
    JsonFormat[TestCaseC]("message", "test-case-c", Some(new TestCaseCSerializer()))
  )
}
