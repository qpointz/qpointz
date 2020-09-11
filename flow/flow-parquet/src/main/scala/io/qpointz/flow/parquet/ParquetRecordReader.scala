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

import io.qpointz.flow.{OperationContext, Record, RecordReader}
import org.apache.avro.mapred.AvroRecordReader
import org.apache.parquet.ParquetReadOptions
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.example.data.Group
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.io.{ColumnIOFactory, InputFile}

class ParquetRecordReaderSettings {
  var inputFile : InputFile = _
}

class ParquetRecordReader(private val settings:ParquetRecordReaderSettings)(implicit val ctx:OperationContext) extends RecordReader {

  override def iterator: Iterator[Record] = {
    val options = ParquetReadOptions.builder().build()
    val reader = ParquetFileReader.open(settings.inputFile, options)
    val footer = reader.getFooter
    val schema = footer.getFileMetaData.getSchema

    def groupToRecordIter(group:Group):Iterator[Record] = {
      //schema.getColumns.get(0)
      //println(group.getType)
      Iterator[Record]()
    }

    def recordGroupToRecordIter(pages:PageReadStore):Iterator[Record] = {
      val columnIo = new ColumnIOFactory().getColumnIO(schema)
      val recordReader = columnIo.getRecordReader(pages, new GroupRecordConverter(schema))
      LazyList
          .continually(recordReader.read())
          .takeWhile(_!=null)
          .map(groupToRecordIter)
          .foldLeft(Iterator[Record]())(_ ++ _)
    }

    LazyList
      .continually(reader.readNextRowGroup())
      .takeWhile(_!=null)
      .map(recordGroupToRecordIter)
      .foldLeft(Iterator[Record]())(_++_)
  }

}
