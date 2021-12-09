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

package io.qpointz.flow.orientdb

import com.orientechnologies.orient.core.record.impl.ODocument
import org.json4s.JObject
import org.json4s.JsonAST.{JArray, JBool, JInt, JNothing, JNull, JValue}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OElementMethodsTest extends AnyFlatSpec with Matchers {
  import org.json4s.JsonDSL._
  import OElementMethods._

  behavior of "JNodeMethods"

  it should "converts int" in {
    val obj:JValue = ("num" -> 12)
    val doc = obj.asODocument
    doc.field[Int]("num") shouldBe 12
  }

  it should "convert double" in {
    val obj:JObject = ("dbl" -> 12.1d)
    obj.asODocument.field[Double]("dbl") shouldBe 12.1
  }

  it should "convert booleans" in {
    val obj:JObject = ("bl" -> true)
    obj.asODocument.field[Boolean]("bl") shouldBe true
  }

  it should "convert strings" in {
    val obj: JObject = ("foo" -> "bar")
    obj.asODocument.field[String]("foo") shouldBe "bar"
  }

  it should "convert Some options" in {
    val obj: JObject = ("oi" -> Some(2))
    obj.asODocument.field[Int]("oi") shouldBe 2
  }

  it should "convert None options" in {
    val o:Option[Int] = None
    val obj: JObject = ("oi" -> o)
    assert(obj.asODocument.field[Any]("oi") == null)
  }

  it should "convert nulls" in {
    val obj: JObject = JObject(List(("nl",JNull)))
    assert(obj.asODocument.field[Any]("nl") == null)
  }

  it should "convert nothing" in {
    val obj: JObject = JObject(List(("noth",JNothing)))
    obj.asODocument.containsField("noth") shouldBe false
  }

  it should "convert nested objects" in {
    val obj : JObject = ("name" -> "foo") ~~ ("nested" -> (("a"-> 1) ~~ ("b"->2)))
    val od = obj.asODocument
    od.containsField("nested") shouldBe true
    val nd = od.field[ODocument]("nested")
    assert(nd !=null) // scalastyle:ignore null
    nd.field[Int]("a") shouldBe(1)
  }

  it should "convert arrays" in {
    val obj: JObject = "arr" -> List(1,2,3)
    obj.asODocument.field[Array[Int]]("arr") should contain allElementsOf(List(1,2,3))
  }

  it should "fail when convert arrays" in {
    val arr: JArray = JArray(List(JInt(1), JInt(2), JInt(3)))
    assertThrows[java.lang.RuntimeException] {
      arr.asODocument
    }
  }

  it should "fail if non document" in {
    val ji:JInt = JInt(2)
    assertThrows[java.lang.RuntimeException] {
      ji.asODocument
    }
  }

  it should "implicit conversions" in {
    val obj:JObject = ("foo"->"bar")
    val doc:ODocument = obj
    doc.field[String]("foo") shouldBe "bar"
  }

}
