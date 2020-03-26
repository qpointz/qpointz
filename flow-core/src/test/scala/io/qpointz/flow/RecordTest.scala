/*
 * Copyright  2019 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.qpointz.flow

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RecordTest extends AnyFlatSpec with Matchers {

  import Record._

  val extraMeta = Seq(("group2", "item2.1", "ho"))

  val baseMeta = Seq(
    ("group.1", "item.1.1", 1),
    ("group.1", "item.1.2", 2)
  )

  val r = Record(Map("a"->"foo", "b"->"bar", "c"->300), baseMeta)

  val combinedMeta = (baseMeta ++ extraMeta).toSet

  "keysSet" should "return attributes set" in {
    r.keySet shouldBe(Set("a","b","c"))
  }

  "keys" should "return attributes iterable" in {
    r.keys shouldBe Set("a","b","c")
  }

  "contains" should "return true if key exists" in {
    r.contains("a") shouldBe true
  }

  it should "return false on missing keys" in {
    r.contains("dummy-key") shouldBe false
  }

  behavior of "getOp(key) "

  it should "return existing key" in {
    r.getOp("a") shouldBe Some("foo")
  }

  it should "return None for missing key" in {
    r.getOp("missing") shouldBe None
  }

  behavior of "get(key)"

  it should "return on existing key" in {
    r.get("b") shouldBe "bar"
  }

  it should "throw on missing key" in {
    assertThrows[NoSuchElementException](r.get("dummy"))
  }

  behavior of "apply(key)"

  it should "return on existing key" in {
    r("b") shouldBe "bar"
  }

  it should "throw on missing key" in {
    assertThrows[NoSuchElementException](r("dummy"))
  }

  behavior of "getOrElse(key, value)"

  it should "return value of existing key" in {
    r.getOrElse("a", "dummy-value") shouldBe("foo")
  }

  it should "return default on missing key" in {
    r.getOrElse("z", "fallback") shouldBe("fallback")
  }

  behavior of "put(...)"

  val putRecord = r.put(Map("a" ->100, "z"->"frank"), extraMeta)

  it should "override existing values" in  {
    putRecord("a") shouldBe 100
  }

  it should "add missing values" in {
    putRecord("z") shouldBe "frank"
  }

  it should "combine meta" in {
    putRecord.meta.toSet shouldBe combinedMeta
  }

  behavior of "set(...)"

  val setRecord = r.set(Map("a"->"100"),extraMeta)

  it should "update existing values" in {
    setRecord("a") shouldBe "100"
  }

  it should "combine meta" in {
    setRecord.meta.toSet shouldBe combinedMeta
  }

  it should "throw on missing keys" in {
    assertThrows[NoSuchElementException](r.set(Map("z"->100),Metadata.empty))
  }

  behavior of "append"

  val appendRecord = r.append(Map("a"->100, "z"->"bar"),extraMeta)
  it should "add missing values" in {
    appendRecord("z") shouldBe "bar"
  }

  it should "retain existing values" in {
    appendRecord("a") shouldBe "foo"
  }

  it should "combine meta" in {
    setRecord.meta.toSet shouldBe combinedMeta
  }

  behavior of "drop"

  val dropRecord = r.drop(Seq("a", "z"),extraMeta)
  it should "drop existing values" in {
    dropRecord.contains("a") shouldBe false
    dropRecord.keySet shouldBe Set("b","c")
  }

  it should "combine meta" in {
    setRecord.meta.toSet shouldBe combinedMeta
  }

  behavior of "remove"
  val removeRecord = r.remove(Seq("a"),extraMeta)
  it should "retain existing values" in {
    removeRecord.contains("a") shouldBe false
    removeRecord.keySet shouldBe Set("b","c")
  }

  it should "combine meta" in {
    setRecord.meta.toSet shouldBe combinedMeta
  }

  it should "fail on missing keys" in {
    assertThrows[NoSuchElementException](r.remove(Seq("aa","bb"),extraMeta))
  }



}
