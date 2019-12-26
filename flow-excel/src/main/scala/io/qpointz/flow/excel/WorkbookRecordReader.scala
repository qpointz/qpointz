package io.qpointz.flow.excel

import io.qpointz.flow.data.{Metadata, Record, RecordReader}
import org.apache.poi.ss.usermodel.Workbook

class WorkbookRecordReaderSettings {
  var sheets : SheetSelectionSettingsCollection = _
}

case class SheetSelectionSettings(selector:SheetSelector, sheetRecordReaderSettings: SheetRecordReaderSettings)

object WorkbookRecordReader {

}

class WorkbookRecordReader(val workbook: Workbook,
                           val settings: WorkbookRecordReaderSettings,
                           val extraMetadata: Metadata)
  extends RecordReader {

  import WorkbookMethods._

  override def iterator: Iterator[Record] = {
    settings.sheets
      .flatMap(x=> SheetSelector.select(x.selector, workbook.sheets()).map((x,_)))
      .map(x=>new SheetRecordReader(x._2, x._1.sheetRecordReaderSettings, extraMetadata))
      .map(_.iterator)
      .reduce(_ ++ _)
  }

}
