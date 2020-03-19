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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.qpointz.flow

import io.qpointz.flow.{Metadata, MetadataItemOps, MetadataOps}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class MetadataOpsTest extends AnyFlatSpec with Matchers {

  object TestMeta extends MetadataOps("b") {

    implicit class TestMetaOps(m: Metadata) {
      def b1: MetadataItemOps[String] = item[String](m, "b1")

      def b2: MetadataItemOps[Int] = item[Int](m, "b2")
    }

  }

  import TestMeta._

  behavior of "ops object"

  it should "put meta" in {
    val m = List()
      .b1("hello")
      .b2(100)

    m.b2() should be (100)
  }



}