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

class HostnameTest extends AnyFlatSpec with Matchers {

  import TransformationsMeta._
  import io.qpointz.flow.MetadataMethods._

  val r = Record("a"->1, "b"->2)
  val lns = LocalhostNameShort("hostname").transform(r)

  "LocalhostNameShort" should "return value" in  {
      lns.attributes("hostname").toString should not be empty
  }

  it should "return metadata" in {
    lns.meta(hostnameShort) shouldBe "hostname"
  }

  val lnc = LocalhostNameCanonical("hostname-canonical").transform(r)

  "LocalHostNameCanonical" should "return value" in {
    lnc.attributes("hostname-canonical").toString should not be empty
  }

  it should "return metadata" in {
    lnc.meta(hostnameCanonical) shouldBe "hostname-canonical"
  }

}
