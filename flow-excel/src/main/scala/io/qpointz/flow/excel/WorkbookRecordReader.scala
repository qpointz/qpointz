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

package io.qpointz.flow.excel

import io.qpointz.flow.{Metadata, Record, RecordReader}
import org.apache.poi.ss.usermodel.Workbook

class WorkbookRecordReaderSettings {
  var sheets : SheetSelectionSettingsCollection = _
}

case class SheetSelectionSettings(selector:SheetSelector, sheetRecordReaderSettings: SheetRecordReaderSettings)

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
