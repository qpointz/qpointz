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
import io.qpointz.flow.avro.AvroSchemaSource
import io.qpointz.flow.{OperationContext, Record, RecordWriter}
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.column.ParquetProperties
import org.apache.parquet.hadoop.{ParquetFileWriter, ParquetWriter}
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.parquet.avro.AvroParquetWriter

class AvroParquetRecordWriterSettings {
  var maxPaddingSize: Int = ParquetWriter.MAX_PADDING_SIZE_DEFAULT
  var writerVersion: ParquetProperties.WriterVersion = ParquetProperties.DEFAULT_WRITER_VERSION
  var dictionaryPageSize: Int = ParquetProperties.DEFAULT_DICTIONARY_PAGE_SIZE
  var dictionaryEncoding: Boolean = ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED
  var validation: Boolean = ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED
  var rowGroupSize: Int = ParquetWriter.DEFAULT_BLOCK_SIZE
  var pageSize: Int = ParquetWriter.DEFAULT_PAGE_SIZE
  var compressionCodec: CompressionCodecName = ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME
  var mode:ParquetFileWriter.Mode = ParquetFileWriter.Mode.CREATE

  var configuration: Configuration = new Configuration(true)
  var schema:AvroSchemaSource = _
  var path:Path = _

  def path(p:String):Unit = {
    path = new Path(p)
  }

}

class AvroParquetRecordWriter(settings:AvroParquetRecordWriterSettings) extends RecordWriter {
  override def open(): Unit = {

  }

  private lazy val schema = settings.schema.avroSchema()

  lazy val writer = AvroParquetWriter.builder[GenericRecord](settings.path)
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
