package io.qpointz.flow.excel

import org.scalatest.{FlatSpec, Matchers}

class SheetRecordReaderTest extends FlatSpec with Matchers {

  val testColumns = List(
    SheetColumn(0, "id"),
    SheetColumn(1, "first_name"),
    SheetColumn(2, "last_name"),
    SheetColumn(3, "email"),
    SheetColumn(4, "gender"),
    SheetColumn(5, "birthday"),
    SheetColumn(6, "registration"),
    SheetColumn(7, "orders"),
    SheetColumn(8, "sum"),
    SheetColumn(9, "rate")
  )

  val testSheet = ExcelTestMethods.sheet("TestData","TestData")

  val testSettings = new SheetRecordReaderSettings()
  testSettings.recordTags = Set("TESTDATA_RECORD")
  testSettings.columns = testColumns

  behavior of "iterator"

  it should "return records" in {
    val td = new SheetRecordReader(testSheet, testSettings, List())
    val recs = td.toSeq
    recs.length should be > 0
  }

}
