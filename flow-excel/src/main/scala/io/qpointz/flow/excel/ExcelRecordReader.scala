package io.qpointz.flow.excel

import io.qpointz.flow.data.{Record, RecordReader}

class ExcelRecordReaderSettings {
  var recordSelector : RecordSelector = _
}

class ExcelRecordReader(private val settings:ExcelRecordReaderSettings) extends RecordReader {

  override def iterator: Iterator[Record] = new Iterator[Record] {
    
    override def hasNext: Boolean = ???

    override def next(): Record = ???
  }
}


