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

package io.qpointz.flow.ql

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class SqlTest extends AnyFlatSpec with Matchers {

  behavior of "expression parse"

  it should "parse function call" in {
    SqlExp("FOO(`a`, 2)") shouldBe FunctionCall("FOO", Seq(Attribute("a"), Constant(2d)))
  }

  it should "parse attributes" in {
    SqlExp("`a`") shouldBe Attribute("a")
  }

  it should "parse metadata calls" in {
    SqlExp("`:g`.t") shouldBe MetadataEntry("g","T")
  }

  it should "parse constants" in {
    SqlExp("1") shouldBe Constant(1d)
  }

  behavior of "statement parse"

  it should "parse alias" in {
    SqlStm("select a as b").select.exp.head.alias shouldBe Some("B")
  }

  it should "parse attribute" in {
    SqlStm("select Col").select.exp.head.alias shouldBe Some("COL")
  }

  it should "parse exp with no name" in {
    SqlStm("select ABS(1)").select.exp.head.alias shouldBe None
  }
}