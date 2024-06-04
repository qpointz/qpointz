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

package io.qpointz.flow.catalogue

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths
import scala.util.Success

class LocalCatalogueTest extends AnyFlatSpec with Matchers {

  val gc = new LocalCatalogue(Paths.get("../etc/data/catalogues/good"))

  behavior of "local catalogue"

  it should "return source" in {
      val rr = gc.source("sample", "inmem")
      rr.toSeq.length>0 shouldBe true
  }

  it should "query records" in {
    val trySql = gc.runSql("select `a`, `b` from `sample`.`inmem`")
    trySql.isSuccess shouldBe true
    trySql.get.toList.length>0 shouldBe true
  }
}
