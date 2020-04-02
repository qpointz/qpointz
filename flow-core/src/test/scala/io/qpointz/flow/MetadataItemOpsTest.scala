/*
 * Copyright 2019 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distribuit should "add value" in {
    var m1:Metadata = List()
    var o = new MetadataItemOps[Int](m1, "a", "a")
    m1 = o.put(100)
    o = new MetadataItemOps[Int](m1, "a", "a")
    o.get() should be (100)
  }ted on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.qpointz.flow

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetadataItemOpsTest extends AnyFlatSpec with Matchers {

  import MetadataMethods._

  behavior of "getOp"

  val m:Metadata = Seq(
    ("a", "a1", 10),
    ("a", "a2", "foo"),
    ("a", "a3", true)
  )

  it should "return value" in {
    val df = EntryDefinition[Int]("a", "a1")
    m.getOp(df) should be(Some(10))
  }

  it should "return none on type mismatch" in {
    val df = EntryDefinition[Int]("a", "a2")
    m.getOp(df) should be(None)
  }

  it should "return none on missing key" in {
    val df = EntryDefinition[Int]("a", "a22")
    m.getOp(df) should be(None)
  }

  it should "return none on missing group" in {
    val df = EntryDefinition[Int]("b", "a1")
    m.getOp(df) should be(None)
  }

  behavior of "get"

  it should "return value" in {
    val df = EntryDefinition[Int]("a", "a1")
    m.get(df) should be(10)
  }

  it should "throw on missing value" in {
    val df = EntryDefinition[Int]("b", "c2")
    the[NoSuchElementException] thrownBy (m.get(df))
  }

  behavior of "getOr"

  it should "return value" in {
    val df = EntryDefinition[Int]("a", "a1")
    m.getOr(df, -1) should be(10)
  }

  it should "fallback value on missing value" in {
    val df = EntryDefinition[Int]("a", "a999")
    m.getOr(df, -1) should be(-1)
  }

  behavior of "put"

  it should "add value" in {
    val df = EntryDefinition[Int]("a", "a")
    var m1:Metadata = Seq()
    m1 = m1.put(df, 100)
    m1.get(df) should be (100)
  }

}
