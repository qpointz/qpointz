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

package io.qpointz.flow.parquet

import io.qpointz.flow.RecordWriter
import io.qpointz.flow.avro.{AvroRecordWriter, ConstantAvroScemaSource}
import io.qpointz.flow.parquet._
import org.apache.avro.SchemaBuilder
import org.apache.commons.io.FileUtils
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.AvroParquetWriter
import org.json4s.Extraction
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Paths}

class JsonSerializationTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    val outDir = Paths.get("./.test/pq-writer")
    if (Files.exists(outDir)) {
      FileUtils.deleteDirectory(outDir.toFile)
    }
    FileUtils.forceMkdir(outDir.toFile)
  }

  val as = new ConstantAvroScemaSource(SchemaBuilder
    .record("default")
    .fields()
    .requiredString("a")
    .requiredString("b")
    .endRecord()
  )

  behavior of "RecordWriter serialization"

  import io.qpointz.flow.serialization.Json._
  import org.json4s.jackson.JsonMethods._
  implicit val fmts = formats

  val st = AvroParquetRecordWriterSettings(as, new Path(".test/avro-writer/writeavro.avro"))
  val w = new AvroParquetRecordWriter(st)


  it should "serialize" in {
    val cnt = pretty(Extraction.decompose(w))
    val nw = Extraction.extract[AvroParquetRecordWriter](parse(cnt))
    nw.settings.schema.avroSchema() shouldBe w.settings.schema.avroSchema()
    nw.settings.path shouldBe w.settings.path
  }

  it should "serialize polymorphic" in {
    val cnt = pretty(Extraction.decompose(w))
    val nw = Extraction.extract[RecordWriter](parse(cnt)).asInstanceOf[AvroRecordWriter]
    nw.settings.schema.avroSchema() shouldBe w.settings.schema.avroSchema()
    nw.settings.path shouldBe w.settings.path
  }

}
