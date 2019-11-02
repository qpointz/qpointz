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

package io.qpointz.flow.data

import org.scalatest.{FlatSpec, Matchers}

class SeqRecordTest extends RecordBaseTest {
  override val r = SeqRecord(Seq("a"->1, "b"-> "bar"))
}

class MapRecordTest extends RecordBaseTest {
  override val r = MapRecord(Map("a"->1, "b"-> "bar"))
}

abstract class RecordBaseTest extends FlatSpec with Matchers {

  val r:Record

  behavior of "get(idx)"

  it should "return when exists" in {
    r.get(0) shouldBe 1
  }

  it should "throw on invalid index" in {
    assertThrows[IndexOutOfBoundsException](r.get(4))
  }

  behavior of "get(key)"

  it should "return on existing key" in {
    r.get("b") shouldBe "bar"
  }

  it should "throw on missing key" in {
    assertThrows[NoSuchElementException](r.get("dummy"))
  }

}
