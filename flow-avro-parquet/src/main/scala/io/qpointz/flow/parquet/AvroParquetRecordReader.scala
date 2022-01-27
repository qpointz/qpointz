/*
 * Copyright 2020 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.flow.parquet

import io.qpointz.flow.utils.IteratorExtensions
import io.qpointz.flow.{MetadataMethods, OperationContext, Record, RecordReader}
import org.apache.avro.generic.GenericRecord
import org.apache.parquet.avro.AvroParquetReader
import org.apache.parquet.io.InputFile
import scala.jdk.CollectionConverters._

class AvroParquetRecordReaderSettings {
  var inputFile : InputFile = _
}

class AvroParquetRecordReader(settings:AvroParquetRecordReaderSettings)(implicit val ctx:OperationContext) extends RecordReader {

  private lazy val reader = AvroParquetReader
    .builder[GenericRecord](settings.inputFile)
    .build()

  override def iterator: Iterator[Record] = {

    def toRecord(gr:GenericRecord):Record = {
      val vals = gr.getSchema.getFields.asScala.map(x=> {
        x.name() -> gr.get(x.pos())
      }).toMap
      Record(vals, MetadataMethods.empty)
    }

    IteratorExtensions
      .lazyList(()=>reader.read())(_!=null)
      .map(toRecord)
  }
}
