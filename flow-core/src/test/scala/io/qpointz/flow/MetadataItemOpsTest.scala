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

import io.qpointz.flow.{Metadata, MetadataItemOps}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetadataItemOpsTest extends AnyFlatSpec with Matchers {

  behavior of "getOp"

  val m:Metadata = List(
    ("a", "a1", 10),
    ("a", "a2", "foo"),
    ("a", "a3", true),
  )

  it should "return value" in {
    val o = new MetadataItemOps[Int](m, "a", "a1")
    o.getOp should be(Some(10))
  }

  it should "return none on type mismatch" in {
    val o = new MetadataItemOps[Int](m, "a", "a2")
    o.getOp should be(None)
  }

  it should "return none on missing key" in {
    val o = new MetadataItemOps[Int](m, "a", "a22")
    o.getOp should be(None)
  }

  it should "return none on missing group" in {
    val o = new MetadataItemOps[Int](m, "b", "a1")
    o.getOp should be(None)
  }

  behavior of "get"

  it should "return value" in {
    val o = new MetadataItemOps[Int](m, "a", "a1")
    o.get should be(10)
  }

  it should "throw on missing value" in {
    val o = new MetadataItemOps[Int](m, "b", "c2")
    the[NoSuchElementException] thrownBy (o.get())
  }

  behavior of "getOr"

  it should "return value" in {
    val o = new MetadataItemOps[Int](m, "a", "a1")
    o.getOr(-1) should be(10)
  }

  it should "fallback value on missing value" in {
    val o = new MetadataItemOps[Int](m, "a", "a999")
    o.getOr(-1) should be(-1)
  }

  behavior of "put"

  it should "add value" in {
    var m1:Metadata = List()
    var o = new MetadataItemOps[Int](m1, "a", "a")
    m1 = o.put(100)
    o = new MetadataItemOps[Int](m1, "a", "a")
    o.get() should be (100)
  }

  behavior of "apply"

  it should "add and return" in {
    var m1:Metadata = List()
    var o = new MetadataItemOps[Int](m1, "a", "a")
    m1 = o(100)
    o = new MetadataItemOps[Int](m1, "a", "a")
    o() should be (100)
  }

}
