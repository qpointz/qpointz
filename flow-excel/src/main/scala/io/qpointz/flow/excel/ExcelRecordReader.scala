package io.qpointz.flow.excel

import io.qpointz.flow.data.{Record, RecordReader}

class ExcelRecordReaderSettings {
  var recordSelector : RecordSelector = _
}

trait SheetSelector {}
case class SelectSheetByName(sheetName:String) extends SheetSelector
case class SelectSheetByIdx(sheetIdx:Int) extends SheetSelector

trait RegionSelector {}
case class SelectAllRows() extends RecordSelector

case class RecordSelector (sheets : SheetSelector, region : RegionSelector)


class ExcelRecordReader(private val settings:ExcelRecordReaderSettings) extends RecordReader {

  override def iterator: Iterator[Record] = new Iterator[Record] {
    
    override def hasNext: Boolean = ???

    override def next(): Record = ???
  }
}


