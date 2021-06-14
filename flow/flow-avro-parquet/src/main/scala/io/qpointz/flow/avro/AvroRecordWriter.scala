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
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord, GenericRecordBuilder}

import java.nio.file.Path
import AvroMethods._

class AvroRecordWriterSettings {
  var schema:AvroSchemaSource = _
  var path:Path = _
}

class AvroRecordWriter(settings:AvroRecordWriterSettings)(implicit val ctx:OperationContext) extends RecordWriter {

  private lazy val schema = settings.schema.avroSchema()
  private lazy val writer = new GenericDatumWriter[GenericRecord](schema)
  private lazy val dataWriter = new DataFileWriter[GenericRecord](writer)

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
