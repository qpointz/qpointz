/*
 * Copyright 2021 qpointz.io
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
 *
 */

package io.qpointz.flow.avro

import io.qpointz.flow.{Record, RecordWriter}
import org.apache.avro.SchemaBuilder
import org.json4s.Extraction
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths

class AvroRecordWriterTest extends AnyFlatSpec with Matchers {

  val as = new ConstantAvroScemaSource(SchemaBuilder
    .record("default")
    .fields()
    .requiredString("a")
    .requiredString("b")
    .endRecord()
  )

  behavior of "write"

  it should "write simple" in {

    val st = AvroRecordWriterSettings(as, Paths.get("./target/test-out/writeavro.avro"))
    val w = new AvroRecordWriter(st)
    w.open()
    w.write(Record("a" -> "a1", "b" -> "b1"))
    w.write(Record("a" -> "a2", "b" -> "b2"))
    w.write(Record("a" -> "a3", "b" -> "b3"))
    w.close()
  }

  behavior of "serialization"

  import io.qpointz.flow.serialization.Json._
  import org.json4s.jackson.JsonMethods._
  implicit val fmts = formats

  val st = AvroRecordWriterSettings(as, Paths.get("./target/test-out/writeavro.avro"))
  val w = new AvroRecordWriter(st)

  it should "serialize" in {
    val cnt = pretty(Extraction.decompose(w))
    val nw = Extraction.extract[AvroRecordWriter](parse(cnt))
    nw.settings.schema.avroSchema() shouldBe w.settings.schema.avroSchema()
    nw.settings.path shouldBe w.settings.path
  }

  it should "serialize polymorphic" in {
    val cnt = pretty(Extraction.decompose(w))
    println(cnt)
    val nw = Extraction.extract[RecordWriter](parse(cnt)).asInstanceOf[AvroRecordWriter]
    nw.settings.schema.avroSchema() shouldBe w.settings.schema.avroSchema()
    nw.settings.path shouldBe w.settings.path
  }

}
