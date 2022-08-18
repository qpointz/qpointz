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

package io.qpointz.flow.parquet

import io.qpointz.flow.avro.AvroMethods._
import io.qpointz.flow.avro.{AvroRecordWriterSettings, AvroSchemaSource}
import io.qpointz.flow.nio.Path
import io.qpointz.flow.serialization.Json.hint
import io.qpointz.flow.{Record, RecordWriter}
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.column.ParquetProperties
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.parquet.hadoop.util.HadoopOutputFile
import org.apache.parquet.hadoop.{ParquetFileWriter, ParquetWriter}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods.{mapper, parse}

case class AvroParquetRecordWriterSettings(
  schema:AvroSchemaSource,
  path:Path,
  maxPaddingSize: Int = ParquetWriter.MAX_PADDING_SIZE_DEFAULT,
  writerVersion: ParquetProperties.WriterVersion = ParquetProperties.DEFAULT_WRITER_VERSION,
  dictionaryPageSize: Int = ParquetProperties.DEFAULT_DICTIONARY_PAGE_SIZE,
  dictionaryEncoding: Boolean = ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED,
  validation: Boolean = ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
  rowGroupSize: Long = ParquetWriter.DEFAULT_BLOCK_SIZE,
  pageSize: Int = ParquetWriter.DEFAULT_PAGE_SIZE,
  compressionCodec: CompressionCodecName = ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME,
  mode:ParquetFileWriter.Mode = ParquetFileWriter.Mode.CREATE,
  configuration: Configuration = new Configuration(true))
{
}

class AvroParquetRecordWriter(val settings:AvroParquetRecordWriterSettings) extends RecordWriter {
  override def open(): Unit = {

  }

  private lazy val schema = settings.schema.avroSchema()


  lazy val writer = AvroParquetWriter.builder[GenericRecord](HadoopOutputFile.fromPath(new fs.Path(settings.path.uri), settings.configuration))
    .withSchema(schema)
    .withConf(settings.configuration)
    .withCompressionCodec(settings.compressionCodec)
    .withWriterVersion(settings.writerVersion)
    .withPageSize(settings.pageSize)
    .withRowGroupSize(settings.rowGroupSize)
    .withMaxPaddingSize(settings.maxPaddingSize)
    .withDictionaryEncoding(settings.dictionaryEncoding)
    .withDictionaryPageSize(settings.dictionaryPageSize)
    .withWriteMode(settings.mode)
    .withValidation(settings.validation)
    .withConf(settings.configuration)
    .build()

  override def write(r: Record): Unit = {
    val record = r.toGenericRecord(schema)
    writer.write(record)
  }

  override def close(): Unit = {
    writer.close()
  }
}

class ParquetRecordWriterSerializer extends CustomSerializer[AvroParquetRecordWriter] (implicit format => (
  {
    case jo:JObject =>
      val s = (jo \ "settings").extract[AvroParquetRecordWriterSettings]
      new AvroParquetRecordWriter(s)
  },
  {
    case w : AvroParquetRecordWriter =>
      hint[AvroParquetRecordWriter] ~ ("settings" -> Extraction.decompose(w.settings))
  }))


class AvroParquetRecordWriterSettingsSerializer extends CustomSerializer[AvroParquetRecordWriterSettings] (implicit format => (
  {case jo:JObject =>
    val sc = AvroSchemaSource(mapper.writeValueAsString(jo \ "schema"))
    val a = (jo \ "path").extract[String]
    val path = Path(a)
    AvroParquetRecordWriterSettings(sc, path)
  },
  {
    case ws : AvroRecordWriterSettings =>
      ("schema" -> parse(ws.schema.avroSchema().toString(true))) ~ ("path" -> ws.path.toAbsolutePath.toString)
  })
)
