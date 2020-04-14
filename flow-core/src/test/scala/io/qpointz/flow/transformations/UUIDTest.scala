/*
 * Copyright 2020 qpointz.io
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

package io.qpointz.flow.transformations

import io.qpointz.flow.Record
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UUIDTest extends AnyFlatSpec with Matchers {

  import TransformationsMeta._
  import io.qpointz.flow.MetadataMethods._

  val r = Record("a"->1, "b"->2)

  "GenerateUUID" should "return" in {
    val g = GenerateUUID("z")
    g.transform(r) should not be g.transform(r)
  }

  it should "contain meta" in {
    val g = GenerateUUID("z").transform(r)
    g.meta(generateUUID) shouldBe("z")
  }

  it should "retain existing attributes" in {
    val g = GenerateUUID("z").transform(r)
    g.attributes.keySet shouldBe Set("a","b","z")
  }

  "GenerateUUIDString" should "return" in {
    val g = GenerateUUIDString("z")
    g.transform(r) should not be g.transform(r)
  }

  it should "contain meta" in {
    val g = GenerateUUIDString("z").transform(r)
    g.meta(generateUUIDString) shouldBe("z")
  }

  it should "retain existing attributes" in {
    val g = GenerateUUID("z").transform(r)
    g.attributes.keySet shouldBe Set("a","b","z")
  }



}
