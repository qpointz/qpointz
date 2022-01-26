/*
 *  Copyright 2021 qpointz.io
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package io.qpointz.flow.avro

import io.qpointz.flow.{OperationContext, Record, RecordWriter}
import org.apache.avro.file.{CodecFactory, DataFileWriter}
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord, GenericRecordBuilder}

import java.nio.file.Path
import AvroMethods._
import io.qpointz.flow.serialization.Json._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._


case class AvroRecordWriterSettings(schema:AvroSchemaSource, path:Path)

class AvroRecordWriter(val settings:AvroRecordWriterSettings)(implicit val ctx:OperationContext) extends RecordWriter {

  private lazy val schema = settings.schema.avroSchema()
  private lazy val writer = new GenericDatumWriter[GenericRecord](schema)
  private lazy val dataWriter = {
    val w = new DataFileWriter[GenericRecord](writer)
    w.setCodec(CodecFactory.snappyCodec())
  }

  override def open(): Unit = {
    val file = settings.path.toFile
    dataWriter.create(schema, file)
  }

  override def close(): Unit = {
    dataWriter.close()
  }

  override def write(r: Record): Unit = {
    val record = r.toGenericRecord(schema)
    dataWriter.append(record)
  }

}

class AvroRecordWriterSerializer extends CustomSerializer[AvroRecordWriter] (implicit format => (
  {
    case jo:JObject =>
      val s = (jo \ "settings").extract[AvroRecordWriterSettings]
      new AvroRecordWriter(s)
  },
  {
    case w : AvroRecordWriter =>
      hint[AvroRecordWriter] ~ ("settings" -> Extraction.decompose(w.settings))
  }))

class AvroRecordWriterSettingsSerializer extends CustomSerializer[AvroRecordWriterSettings] (implicit format => (
  {case jo:JObject =>
    val sc = AvroSchemaSource(mapper.writeValueAsString(jo \ "schema"))
    val path = Path.of((jo \ "path").extract[String])
    AvroRecordWriterSettings(sc, path)
  },
  {
    case ws : AvroRecordWriterSettings =>
      ("schema" -> parse(ws.schema.avroSchema().toString(true))) ~ ("path" -> ws.path.toString)
  })
)
