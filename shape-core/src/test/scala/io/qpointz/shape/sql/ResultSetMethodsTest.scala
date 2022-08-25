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

package io.qpointz.shape.sql

import io.qpointz.flow.serialization.Json.formats
import io.qpointz.shape.SqlBaseTest
import org.json4s.{DefaultFormats, Extraction}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.sql.ResultSet

class ResultSetMethodsTest extends AnyFlatSpec with Matchers with SqlBaseTest {

  import io.qpointz.shape.sql._

  def rs:ResultSet = executeQuery("select id, country, city, birthday from depts where id<=10")

  behavior of "basemethods"

  it should "asMapSeq" in {
    val ms = rs.asMapSeq
    ms.length shouldBe(10)
    ms.drop(3).head("ID") shouldBe(4)
  }

  it should "asArraySeq" in {
    val ms = rs.asArraySeq
    ms.length shouldBe (10)
    val a = ms.drop(3).head
    a(0) shouldBe (4)
    a(1) shouldBe ("Russia")
    a(2) shouldBe ("Mugi")
  }


  behavior of "CompactMap"

  it should "asCompactMap" in {
    val ms = rs.asCompactMap
    ms.data.length shouldBe(10)
    val a = ms.data.drop(3).head
    a(0) shouldBe(4)
    ms.columns.head shouldBe (0,"ID")
  }

  it should "serialize" in {
    import org.json4s.jackson.JsonMethods._
    implicit val fmts = formats
    val ms = rs.asCompactMap
    val p = pretty(Extraction.decompose(ms))
    val ms2 = Extraction.extract[CompactMap](parse(p))
    val p2 = pretty(Extraction.decompose(ms2))
    p shouldBe(p2)
  }


}
