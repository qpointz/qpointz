/*
 * Copyright 2020 qpointz.io
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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.qpointz.flow.MetadataMethods._

class WorkbookRecordReaderTest extends AnyFlatSpec with Matchers {

  val wb = ExcelTestMethods.workbook("SheetsData")

  val tabASheetSettings = new SheetRecordReaderSettings()
  tabASheetSettings.columns = List(
    SheetColumn(0, "A"),
    SheetColumn(1, "B"),
    SheetColumn(2, "C")
  )

  val tabASettings = SheetSelectionSettings(
    SheetSelector.include(List(
      SheetByNamePattern("TabA.*")
    )),
    tabASheetSettings
  )

  val tabBSheetSettings = new SheetRecordReaderSettings()
  tabBSheetSettings.columns = List(
    SheetColumn(0, "E"),
    SheetColumn(1, "F"),
    SheetColumn(2, "G")
  )

  val tabBSettings = SheetSelectionSettings(
    SheetSelector.include(List(
      SheetByName("TabB1")
    )),
    tabBSheetSettings
  )

  val settings = new WorkbookRecordReaderSettings()
  settings.sheets = List(
    tabASettings,
    tabBSettings
  )

  def reader() = new WorkbookRecordReader(wb, settings, Seq(
    ("Extra", "Key:SubKey", "Foo:Bar")
  ))

  behavior of "reader"

  it should "return all rows" in {
    val records = reader().toList
    records.length should be > 0
  }


}
