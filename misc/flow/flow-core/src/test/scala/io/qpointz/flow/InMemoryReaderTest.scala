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

package io.qpointz.flow

import org.json4s.Extraction
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class InMemoryReaderTest  extends AnyFlatSpec with Matchers  {

  behavior of "reader"

  val recs = List(
    Record("a"->"a1", "b"->"b1"),
    Record("a"->"a2", "b"->"b2"),
    Record("a"->"a3", "b"->"b3"),
    Record("a"->"a4", "b"->"b4"),
  )

  it should "return records" in {
    val inr = new InMemoryReader(recs)
    inr.toList shouldBe recs
  }

  import io.qpointz.flow.serialization.Json._
  implicit val fmts = formats
  import org.json4s.jackson.JsonMethods._

  it should "serialize" in  {
    val inr = new InMemoryReader(recs)
    val jo = Extraction.decompose(inr)
    println(pretty(jo))
    val inr2 = jo.extract[InMemoryReader]
    inr2.records shouldBe recs
  }

}
