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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.net.URI
import java.nio.file.Paths

class PathTest extends AnyFlatSpec with Matchers {

  val dpath = "file:///my/a/b/d.txt"
  val upath = Path(URI.create(dpath))

  behavior of "apply"

  it should "from string" in {
    Path(dpath) shouldBe upath
  }

  it should "from file" in {
    val f = new File(dpath)
    Path(f) shouldBe upath
  }

  it should "from Path" in {
    val p = Paths.get(dpath)
    Path(p) shouldBe upath
  }

}
