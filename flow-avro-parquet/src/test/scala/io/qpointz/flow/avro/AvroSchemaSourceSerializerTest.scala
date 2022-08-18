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

package io.qpointz.flow.avro

import org.apache.avro.SchemaBuilder
import org.apache.avro.reflect.AvroSchema
import org.json4s.Extraction
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AvroSchemaSourceSerializerTest extends AnyFlatSpec with Matchers {

  behavior of "SchemaSerializer"

  lazy val avroSchema = SchemaBuilder.builder()
    .record("default")
    .fields()
    .requiredInt("a")
    .requiredString("b")
    .requiredBoolean("c")
    .endRecord()
  lazy val sc = AvroSchemaSource(avroSchema)

  import io.qpointz.flow.serialization.Json._
  import org.json4s.jackson.JsonMethods._
  implicit val fmts = formats

  it should "serialize" in {
    import io.qpointz.flow.serialization.JsonProtocol._
    val js = Extraction.decompose(sc)
    val s2 = Extraction.extract[AvroSchemaSource](js)
    sc.avroSchema() shouldBe s2.avroSchema()
  }

}
