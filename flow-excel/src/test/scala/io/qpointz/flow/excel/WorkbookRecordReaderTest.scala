package io.qpointz.flow.excel

import org.scalatest.{FlatSpec, Matchers}

class WorkbookRecordReaderTest extends FlatSpec with Matchers {

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

  def reader() = new WorkbookRecordReader(wb, settings, List(
    ("Extra", "Key:SubKey", "Foo:Bar")
  ))

  behavior of "reader"

  it should "return all rows" in {
    val records = reader().toList
    records.length should be > 0
  }


}
