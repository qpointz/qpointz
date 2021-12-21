/*
 * Copyright 2021 qpointz.io
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

package io.qpointz.flow.orientdb.implicits

import com.orientechnologies.orient.core.record.impl.ODocument
import org.json4s.{JField, JObject}
import org.json4s.JsonAST.{JDecimal, JLong}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ODocumentImplicitsTest extends AnyFlatSpec with Matchers {
  import org.json4s.JsonDSL._
  import io.qpointz.flow.orientdb._

  behavior of "ODocument implicits"

  private def td[T](tuple: (String, T)) = {
    import org.json4s.{ Extraction, NoTypeHints }
    import org.json4s.jackson.Serialization
    implicit val formats = Serialization.formats(NoTypeHints)
    val jO = Extraction.decompose(tuple)
    new ODocument()
      .field(tuple._1, tuple._2)
      .asJValue shouldBe jO
  }

  it should "convert strings" in {
    td("foo" -> "bar")
  }

  it should "convert boolean" in {
    td("bl" -> true)
  }


  it should "convert ints" in {
    td("i" -> 12)
  }

  it should "convert bigints" in {
    td("bi" -> BigInt(23))
  }

  it should "convert arrays" in {
    td("arr" -> List("x","y", "z"))
  }

  it should "convert doubles" in {
    td("dbl" -> 1.2d)
  }

  it should "convert big decimal" in {
    val ln = BigDecimal(1222)
    new ODocument()
      .field("lng", ln)
      .asJValue shouldBe JObject(List(("lng", JDecimal(ln))))
  }

  it should "convert long" in {
    val ln = 1222.toLong
    new ODocument()
      .field("lng", ln)
      .asJValue shouldBe JObject(List(("lng", JLong(ln))))
  }

  it should "convert nested documents" in {
    new ODocument()
      .field("nest", new ODocument().field("n", "a"))
      .asJValue shouldBe JObject(List("nest" -> JObject(List(JField("n","a")))))
  }

}
