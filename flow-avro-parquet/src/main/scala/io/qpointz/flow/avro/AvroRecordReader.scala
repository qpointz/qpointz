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

import io.qpointz.flow.serialization.Json._
import io.qpointz.flow.{AttributeKey, AttributeValue, Metadata, OperationContext, Record, RecordReader}
import org.apache.avro.file.DataFileReader
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, JObject}

import java.io.File
import scala.jdk.CollectionConverters._

class AvroRecordReader(val in:File) extends RecordReader {
  import io.qpointz.flow.MetadataMethods._

  override def iterator: Iterator[Record] = {

    val datumReader = new GenericDatumReader[GenericRecord]
    val dataFileReader = new DataFileReader[GenericRecord](in, datumReader)
    val schema = datumReader.getSchema.getFields.asScala.map(x=> (x.pos(), x.name())).toList

    def asRecord(record: GenericRecord): Record = {
      val m: Map[AttributeKey, AttributeValue] = schema
        .map(x=> (x._2 , record.get(x._2)))
        .toMap
      Record(m)
    }

    dataFileReader.iterator().asScala.map(asRecord(_))
  }
}

import org.json4s._

class AvroRecordReaderSerializer extends CustomSerializer[AvroRecordReader](implicit format => (
  {case jo:JObject => {
    val p = (jo \ "source").extract[String]
    new AvroRecordReader(new File(p))
  }},
  {case r:AvroRecordReader =>
      hint[AvroRecordReader] ~ ("source" -> r.in.toPath.toString)
  })
)
