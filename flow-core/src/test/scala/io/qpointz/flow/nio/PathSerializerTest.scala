/*
 *
 *  Copyright 2022 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.flow.nio

import org.json4s.{Extraction, JField, JObject, JString}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI

class PathSerializerTest extends AnyFlatSpec with Matchers {

  implicit val formats = io.qpointz.flow.serialization.Json.formats
  val dpath = "/my/a/b/d.txt"
  val path = Path(URI.create(dpath))

  behavior of "serialization"

  it should "serialize to string" in {
    val jo = Extraction.decompose(path)
    jo shouldBe JString(dpath)
  }

  behavior of "desereliaze"

  it should "from obect" in {
    val js = JObject(List(JField("uri", JString(dpath)))).extract[Path]
    js shouldBe path
  }

  it should "from string" in {
    val js = JString(dpath).extract[Path]
    js shouldBe path
  }

}
