package io.qpointz.flow.excel

import org.scalatest.{FlatSpec, Matchers}


class WorkbookMethodsTest extends FlatSpec with Matchers {

  import WorkbookMethods._
  behavior of "open"

  it should "open workbook by path" in {
    val wb = open("./flow-excel/src/test/resources/flow-excel-test/TestData.xlsx")
    wb.sheets().length  should be > 0
  }



}
