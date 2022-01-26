/*
 * Copyright 2020 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
package io.qpointz.flow.transform

import io.qpointz.flow.{Metadata, MetadataOps, Record}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AttributeTransformsMethodsTest extends AnyFlatSpec with Matchers {


  import AttributeTransformsMethods._
  import AttributeTransformsMetadata._

  behavior of "attribute operations"

  it should "add metadata" in {
    val tr = Record(Map("a"-> 1, "b"->2, "c"->3, "d"->4), Seq(("a","a1", "a1:val")))
    val dr = attributeOperation(tr, true)(
      {m=> m :+ ("b", "b1", "b1:val" )},
      {(r,m) => Record(r, m)}
    )
    dr.metadata shouldBe Seq(("a","a1", "a1:val"),("b", "b1", "b1:val" ))
  }

  it should "retain metadata" in {
    val tr = Record(Map("a"-> 1, "b"->2, "c"->3, "d"->4), Seq(("a","a1", "a1:val")))
    val dr = attributeOperation(tr, false)(
      {m=> m :+ ("b", "b1", "b1:val" )},
      {(r,m) => Record(r, m)}
    )
    dr.metadata shouldBe Seq(("a","a1", "a1:val"))
  }

  it should "apply record transofrmation" in {
    val tr = Record(Map("a"-> 1, "b"->2, "c"->3, "d"->4), Seq(("a","a1", "a1:val")))
    val dr = attributeOperation(tr, false)(
      {m=> m :+ ("b", "b1", "b1:val" )},
      {(r,m) =>
        val vals = r.map{
          case ("a", _) => ("a"-> "bar")
          case (k,v) => (k,v) }.toMap
        Record(vals, m)
      }
    )
    dr.get("a") shouldBe("bar")
  }


  behavior of "dropAttributes"

  it should "remove attributes" in {
    val dr= dropAttributes(
      Record(Map("a"-> 1, "b"->2, "c"->3, "d"->4), Metadata.empty)
      , Set("a", "c"))
    dr.keys should contain ("b")
    dr.keys shouldNot contain("a")
    dr.keys shouldNot contain("c")
  }

  it should "remove single attribute" in {
    val dr= dropAttribute(
      Record("a"-> 1, "b"->2, "c"->3, "d"->4), "a")
    dr.keys should contain ("b")
    dr.keys shouldNot contain("a")
    dr.keys should contain("c")
  }

  behavior of "retain attributes"

  it should "retain only" in {
    val dr = retainAttributes(
      Record("a"-> 1, "b"->2, "c"->3),
      Set("a", "c")
    )

    dr.keys should contain ("a")
    dr.keys should contain ("c")
    dr.keys shouldNot contain ("b")
  }


  /*"addCurrentTime" should "add current time stamp" in {
    assert(false, "Not Implemented")
  }

  "addCurrentDate" should "add current date" in {
    assert(false, "Not Implemented")
  }

  "convertTimestamp" should "convert date from one of provided formats to output format and time zone" in {
  }


  "addLocalHost" should "adds host name" in {
    assert(false, "Not Implemented")
  }

  "addUUID" should "add UUID value" in {
    assert(false, "Not Implemented")
  }*/


  behavior of "upsertValues"

  lazy val upsertValuesTest:Record = {
    val r = Record(Map("a"->1, "b"->2, "c"->3), Seq( ("initial", "meta", "record")))
    upsertValues(r, Map(
      "a" -> (()=>"hello", Seq(("hello", "hello:A"))),
      "z" -> (()=>"world", Seq(("world", "world:Z")))
    ))
  }

  it should "add not present values" in {
     upsertValuesTest.get("z") shouldBe("world")
  }

  it should "override present values" in {
    upsertValuesTest.get("a") shouldBe("hello")
  }

  it should "append metadata per operation" in {
    upsertValuesTest.metadata shouldBe(Seq(
      ("initial", "meta", "record"),
      (AttributeTransformsMetadata.groupKey,"hello", "hello:A"),
      (AttributeTransformsMetadata.groupKey,"world", "world:Z")
    ))
  }

  behavior of "updateValues"

  lazy val updateValuesTest:Record = {
    val r = Record(Map("a1"->1, "b1"->2, "c1"->3), Seq( ("initial", "meta", "record")))
    updateValues(r, Map(
      "a1" -> (()=>"hello a1", Seq(("value:update", "hello:A1"))),
      "z1" -> (()=>"hello z1", Seq(("value:update", "world:Z1")))
    ))
  }

  it should "update existing values" in {
    updateValuesTest.get("a1") shouldBe("hello a1")
  }

  it should "not create absent values" in {
    updateValuesTest.getOp("z1") shouldBe None
  }

  it should "contain metadata only for updated values" in {
    updateValuesTest.metadata shouldBe(Seq(
      ("initial", "meta", "record"),
      (AttributeTransformsMetadata.groupKey,"value:update", "hello:A1"),
    ))
  }


  behavior of "appendValue"

  lazy val appendValuesTest:Record = {
    val r = Record(Map("a1"->1, "b1"->2, "c1"->3), Seq( ("initial", "meta", "record")))
    appendValues(r, Map(
      "a1" -> (()=>"hello a1", Seq(("value:update", "hello:A1"))),
      "z1" -> (()=>"hello z1", Seq(("value:update", "world:Z1")))
    ))
  }


  it should "adds only absent values" in {
    appendValuesTest.get("a1") shouldBe(1)
    appendValuesTest.get("z1") shouldBe("hello z1")
  }

  it should "add only appended values metadata" in {
    appendValuesTest.metadata shouldBe(Seq(
      ("initial", "meta", "record"),
      (AttributeTransformsMetadata.groupKey,"value:update", "world:Z1"),
    ))
  }

  /*
  "decodeBase64" should "convert base64 string to byte array" in {
    assert(false, "Not Implemented")
  }

  "replace" should "replace all occurencies of string" in {
    assert(false, "Not Implemented")
  }

  "replaceRx" should "replace all rx matches in string" in {
    assert(false, "Not Implemented")
  }

  "extractRx" should "extract attributes using regular expression" in {
    assert(false, "Not Implemented")
  }

  "removeValues" should "removes values matching all key->value with value" in {
    assert(false, "Not Implemented")
  }

  "retainValues" should "retain only values matchin all key->value" in {
    assert(false, "Not Implemented")
  }

  "translate value" should "translate value by given dictionary" in {
    assert(false, "Not Implemented")
  }
*/
}
*/