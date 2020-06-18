package io.qpointz.flow.parquet

import io.qpointz.flow.{Record, RecordReader}
import org.apache.parquet.ParquetReadOptions
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.example.data.Group
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.io.{ColumnIOFactory, InputFile}

class ParquetRecordReaderSettings {
  var inputFile : InputFile = _
}

class ParquetRecordReader(private val settings:ParquetRecordReaderSettings) extends RecordReader {

  override def iterator: Iterator[Record] = {
    val options = new ParquetReadOptions()
    val reader = ParquetFileReader.open(settings.inputFile, options)
    val footer = reader.getFooter

    val schema = footer.getFileMetaData.getSchema

    def groupToRecordIter(group:Group):Iterator[Record] = {
      println(group)
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
