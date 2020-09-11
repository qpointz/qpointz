package io.qpointz.flow.excel

import io.qpointz.flow.excel.WorkbookMethods._
import org.apache.poi.ss.usermodel.{Sheet, Workbook}

object ExcelTestMethods {

  def testPath(nm:String):String = s"./flow/flow-excel/src/test/resources/flow-excel-test/${nm}.xlsx"

  def workbook(nm:String):Workbook = {
    open(testPath(nm))
  }

  def sheet(nm:String, st:String):Sheet = {
    workbook(nm).getSheet(st)
  }

}
